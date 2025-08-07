package com.alibaba.agentic.core.models;

import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhenkui.yzk
 * 阿里百炼Api说明:https://help.aliyun.com/zh/model-studio/use-qwen-by-calling-api
 * 使用SDK方式接入
 */
@Slf4j
@Component
public class DashScopeLlm implements BasicLlm {

    private String apiKey;

    @Override
    public String model() {
        return "dashscope";
    }

    private static Role mapRole(String role) {
        if (role == null) {
            return Role.USER;
        }
        return switch (role.toLowerCase()) {
            case "user" -> Role.USER;
            case "assistant" -> Role.ASSISTANT;
            case "system" -> Role.SYSTEM;
            default -> Role.USER;
        };
    }


    private List<Message> toQwenMessages(LlmRequest llmRequest) {
        return llmRequest.getMessages().stream()
                .map(m -> Message.builder()
                        .role(mapRole(m.getRole()).getValue())
                        .content(m.getContent())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private LlmResponse toLlmResponse(GenerationResult result) {
        LlmResponse response = new LlmResponse();

        if (result == null) {
            return response;
        }
        response.setId(result.getRequestId());

        // Usage
        if (result.getUsage() != null) {
            LlmResponse.Usage usage = new LlmResponse.Usage();
            usage.setPromptTokens(result.getUsage().getInputTokens());
            usage.setCompletionTokens(result.getUsage().getOutputTokens());
            usage.setTotalTokens(result.getUsage().getTotalTokens());
            response.setUsage(usage);
        }

        // Choices
        if (result.getOutput() != null && result.getOutput().getChoices() != null) {
            List<LlmResponse.Choice> choices = result.getOutput().getChoices().stream().map(choice -> {
                LlmResponse.Choice c = new LlmResponse.Choice();
                if (choice.getMessage() != null) {
                    c.setText(choice.getMessage().getContent());
                    LlmResponse.Message m = new LlmResponse.Message();
                    m.setRole(choice.getMessage().getRole());
                    m.setContent(choice.getMessage().getContent());
                    c.setMessage(m);
                }
                c.setFinishReason(choice.getFinishReason());
                c.setIndex(choice.getIndex());
                return c;
            }).collect(Collectors.toList());
            response.setChoices(choices);
        }

        return response;
    }


    @Override
    public Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext) {
        List<Message> messages = toQwenMessages(llmRequest);

        if (InvokeMode.SSE.equals(systemContext.getInvokeMode())) {
            return invokeStream(llmRequest);
        }
        GenerationParam param = GenerationParam.builder()
                .model(llmRequest.getModelName())
                .apiKey(getApiKey())
                .messages(messages)
                .parameters((Map<? extends String, ?>) llmRequest.getExtraParams())
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        return Flowable.fromCallable(() -> {
            try {
                Generation gen = new Generation();
                GenerationResult result = gen.call(param);
                return toLlmResponse(result);
            } catch (Throwable e) {
                throw new RuntimeException("Qwen 调用失败", e);
            }
        });

    }

    public Flowable<LlmResponse> invokeStream(LlmRequest llmRequest) {
        List<Message> messages = toQwenMessages(llmRequest);

        GenerationParam param = GenerationParam.builder()
                .model(llmRequest.getModelName())
                .apiKey(getApiKey())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .parameters((Map<? extends String, ?>) llmRequest.getExtraParams())
                .incrementalOutput(true)
                .build();

        return Flowable.create(emitter -> {
            try {
                Generation gen = new Generation();
                io.reactivex.Flowable<GenerationResult> stream = gen.streamCall(param);
                stream.blockingForEach(r -> emitter.onNext(toLlmResponse(r)));
                emitter.onComplete();
            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                emitter.onError(e);
            }
        }, BackpressureStrategy.BUFFER);
    }

    private String getApiKey() {
        if (apiKey == null) {
            apiKey = PropertyConstant.dashscopeApiKey;
        }
        return apiKey;
    }
}