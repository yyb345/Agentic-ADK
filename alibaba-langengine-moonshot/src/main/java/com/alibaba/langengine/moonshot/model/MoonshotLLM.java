/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.moonshot.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.moonshot.model.completion.CompletionChunk;
import com.alibaba.langengine.moonshot.model.completion.CompletionRequest;
import com.alibaba.langengine.moonshot.model.completion.CompletionResult;
import com.alibaba.langengine.moonshot.model.service.MoonshotService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.Proxy;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.moonshot.model.Config.MOONSHOT_API_KEY;


@Data
@Slf4j
public class MoonshotLLM extends BaseLLM<ChatCompletionRequest> {

    private static final String DEFAULT_MODEL = "moonshot-v1-8k";
    private static final String SUFFIX = ": ";
    private String apiVersion;
    private MoonshotService service;
    private Boolean stream = false;

    // 可选 moonshot-v1-8k,moonshot-v1-32k,moonshot-v1-128k 其一，默认moonshot-v1-8k
    private String model;

    // ---------- 以下为选填内容 -------------
    /** 选填，使用什么采样温度，介于 0 和 1 之间。较高的值（如 0.7）将使输出更加随机，而较低的值（如 0.2）将使其更加集中和确定性 */
    private Double temperature;

    /** 选填，另一种采样方法，即模型考虑概率质量为 top_p 的标记的结果。因此，0.1 意味着只考虑概率质量最高的 10% 的标记。一般情况下，我们建议改变这一点或温度，但不建议 同时改变 */
    private Double topP;

    /** 选填，聊天完成时生成的最大 token 数。如果到生成了最大 token 数个结果仍然没有结束，finish reason 会是 "length", 否则会是 "stop"*/
    private Integer maxTokens;

    /** 选填，为每条输入消息生成多少个结果。 默认为 1，不得大于 5。特别的，当 temperature 非常小靠近 0 的时候，我们只能返回 1 个结果*/
    private Integer n;

    /** 选填，存在惩罚，介于-2.0到2.0之间的数字。正值会根据新生成的词汇是否出现在文本中来进行惩罚，增加模型讨论新话题的可能性.默认为 0 */
    private Double presencePenalty;

    /** 选填，频率惩罚，介于-2.0到2.0之间的数字。正值会根据新生成的词汇在文本中现有的频率来进行惩罚，减少模型一字不差重复同样话语的可能性.默认为 0 */
    private Double frequencyPenalty;

    public MoonshotLLM(){
        this(null);
    }

    public MoonshotLLM(String apiKey){
        this(null,null,null,apiKey,null);
    }

    public MoonshotLLM(String apiVersion, String serverUrl, Long timeout, String apiKey, Proxy proxy){
        setApiVersion(StringUtils.isBlank(apiVersion)?Config.MOONSHOT_API_VERSION:apiVersion);
        serverUrl = StringUtils.isBlank(serverUrl)?Config.MOONSHOT_SERVER_URL:serverUrl;
        timeout = (timeout == null?Long.parseLong(Config.MOONSHOT_SERVER_TIMEOUT):timeout);
        apiKey = (StringUtils.isBlank(apiKey)? MOONSHOT_API_KEY:apiKey);
        setModel(DEFAULT_MODEL);
        if(StringUtils.isBlank(apiKey)){
            throw new RuntimeException("api_key must be specified.");
        }
        service = new MoonshotService(serverUrl, Duration.ofSeconds(timeout),apiKey,proxy);
    }
    @Override
    public String run(String prompt, List<String> stop, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        List<CompletionRequest.RoleContent> message = getMessasg(Arrays.asList(prompt));
        CompletionRequest request = CompletionRequest.builder()
                .stream(stream)
                .frequencyPenalty(frequencyPenalty)
                .messages(message)
                .n(n)
                .stop(stop)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .presencePenalty(presencePenalty)
                .model(model)
                .topP(topP)
                .build();
        List<String> answerContentList = new ArrayList<>();
        if(stream ){
            service.createCompletionStream(apiVersion,request)
                    .doOnError(Throwable::printStackTrace)
                    .blockingForEach(e -> {

                        log.info("moonshot answer:" + JSON.toJSONString(e));
                        String answer = Optional.of(e)
                                .map(CompletionChunk::getChoices)
                                .filter(t->t.size() > 0)
                                .map(t->t.get(0))
                                .map(CompletionChunk.Choice::getDelta)
                                .map(CompletionRequest.RoleContent::getContent)
                                .orElse(null);
                        if (answer != null && StringUtils.isNotEmpty(answer)) {
                            answerContentList.add(answer);
                            if(consumer != null){
                                consumer.accept(answer);
                            }
                        }

                    });
        }else{
            CompletionResult completionResult = service.createCompletion(apiVersion, request);
            log.info("moonshot answer:" + JSON.toJSONString(completionResult));
            String answer = Optional.of(completionResult)
                    .map(CompletionResult::getChoices)
                    .filter(t->t.size() > 0)
                    .map(t->t.get(0))
                    .map(CompletionResult.Choice::getMessage)
                    .map(CompletionRequest.RoleContent::getContent)
                    .orElse(null);
            if (answer != null) {
                answerContentList.add(answer);
            }
        }

        String responseContent = answerContentList.stream().collect(Collectors.joining(""));
        responseContent = responseContent.replace(prompt + "\n\n", "");
        return responseContent;
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public String runRequest(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }

    @Override
    public String runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        return "";
    }

    private List<CompletionRequest.RoleContent> getMessasg(List<String> prompts) {
        List<CompletionRequest.RoleContent> list = new ArrayList<>();
        for(String prompt:prompts){
            // 当前moonsht需提供，且只支持这几个角色
            CompletionRequest.RoleContent roleContent = new CompletionRequest.RoleContent();
            list.add(roleContent);
            if(prompt.startsWith(MessageConverter.AI_PREFIX + SUFFIX) ){
                roleContent.setContent(prompt.substring((MessageConverter.AI_PREFIX + SUFFIX).length()));
                roleContent.setRole(CompletionRequest.Role.assistant.name());
            }else if(prompt.startsWith(MessageConverter.HUMAN_PREFIX + SUFFIX)){
                roleContent.setContent(prompt.substring((MessageConverter.HUMAN_PREFIX + SUFFIX).length()));
                roleContent.setRole(CompletionRequest.Role.user.name());
            }else if(prompt.startsWith(MessageConverter.SYSTEM_PREFIX + SUFFIX)){
                roleContent.setContent(prompt.substring((MessageConverter.SYSTEM_PREFIX + SUFFIX).length()));
                roleContent.setRole(CompletionRequest.Role.system.name());
            }else{
                roleContent.setContent(prompt);
                roleContent.setRole(CompletionRequest.Role.user.name());
            }
        }
        return list;
    }


}
