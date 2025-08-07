/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.models.BasicLlm;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESCRIPTION
 * llm管理、调用代理
 *
 * @author baliang.smy
 * @date 2025/7/8 16:02
 */
@Component
@Slf4j
public class DelegationLlm extends FrameworkDelegationBase {

    public final static Map<String, BasicLlm> llmMap = new ConcurrentHashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * llm注册
     *
     * @param llm
     */
    public static void register(BasicLlm llm) {
        if (llmMap.containsKey(llm.model())) {
            log.warn(String.format("duplicated model name of %s", llm.model()));
            return;
        }
        llmMap.put(llm.model(), llm);
    }

    @PostConstruct
    public void init() {
        ServiceLoader<BasicLlm> loader = ServiceLoader.load(BasicLlm.class);
        for (BasicLlm llm : loader) {
            if (llmMap.containsKey(llm.model())) {
                log.warn(String.format("duplicated model of %s", llm.model()));
                return;
            }
            llmMap.put(llm.model(), llm);
        }
    }

    /**
     * 获取llm
     *
     * @param model
     * @return
     */
    protected BasicLlm getModel(String model) {
        if (MapUtils.isEmpty(llmMap) || !llmMap.containsKey(model)) {
            throw new BaseException(String.format("model:%s is not exits.", model), ErrorEnum.SYSTEM_ERROR);
        }
        return llmMap.get(model);
    }


    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        try {
            LlmRequest llmRequest = new JSONObject(request.getParam()).toJavaObject(LlmRequest.class);
            BasicLlm basicLlm = getModel(llmRequest.getModel());
            return basicLlm.invoke(llmRequest, systemContext)
                    .map(llmResponse -> {
                        boolean success = llmResponse.getError() == null;
                        if (success) {
                            return Result.success(mapper.convertValue(llmResponse, new TypeReference<>() {}));
                        }
                        throw new BaseException(llmResponse.getError().getMessage(), ErrorEnum.SYSTEM_ERROR);
                    })
                    .onErrorReturn(Result::fail);
        } catch (Throwable throwable) {
            return Flowable.fromCallable(() -> Result.fail(throwable));
        }

    }

    @Override
    public Map<String, Object> generateRequest(ExecutionContext executionContext, String activityId) {
        Map<String, Object> properties = super.generateRequest(executionContext, activityId);
        if (MapUtils.isEmpty(properties)) {
            return Map.of();
        }
        LlmRequest request = new LlmRequest();
        request.setModelName((String) properties.get("modelName"));
        request.setModel((String) properties.get("model"));
        if (properties.containsKey("maxTokens")) {
            request.setMaxTokens(Integer.valueOf(properties.get("maxTokens").toString()));
        }
        if (properties.containsKey("temperature")) {
            request.setTemperature(Double.valueOf(properties.get("temperature").toString()));
        }
        if (properties.containsKey("topP")) {
            request.setTopP(Double.valueOf(properties.get("topP").toString()));
        }
        if (properties.containsKey("stop")) {
            // 假设 stop 是 List<String>，要保证类型转换正确
            Object stopValue = properties.get("stop");
            if (stopValue instanceof List) {
                request.setStop((List<String>) stopValue);
            } else if (stopValue instanceof String) {
                // 可能是 ["a","b"] 这种字符串，需要解析
                request.setStop(JSONArray.parseArray((String) stopValue, String.class));
            }
        }
        if (properties.containsKey("stream")) {
            request.setStream(Boolean.valueOf(properties.get("stream").toString()));
        }
        if (properties.containsKey("user")) {
            request.setUser((String) properties.get("user"));
        }
        if (properties.containsKey("messages")) {
            // messages 可能是 JSON 字符串或 List
            Object messagesObj = properties.get("messages");
            if (messagesObj instanceof List) {
                request.setMessages((List<LlmRequest.Message>) messagesObj);
            } else if (messagesObj instanceof String) {
                request.setMessages(JSONArray.parseArray((String) messagesObj, LlmRequest.Message.class));
            }
        }
        if (properties.containsKey("extraParams")) {
            request.setExtraParams(properties.get("extraParams"));
        }
        // 关键：**封装成对象再序列化！**
        return JSONObject.parseObject(JSONObject.toJSONString(request));
    }

}
