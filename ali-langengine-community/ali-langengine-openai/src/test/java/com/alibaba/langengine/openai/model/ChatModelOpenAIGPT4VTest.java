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
package com.alibaba.langengine.openai.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageContent;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.util.ImageUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageConstant.CHAT_MESSAGE_CONTENTS_KEY;

public class ChatModelOpenAIGPT4VTest {

    @Test
    public void test_gpt4v() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setMaxTokens(1024);

        List<BaseMessage> messages = new ArrayList<>();

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setAdditionalKwargs(new HashMap<>());

        List<ChatMessageContent> chatMessageContents = new ArrayList<>();
        ChatMessageContent chatMessageContent = new ChatMessageContent();
        chatMessageContent.setType("text");
//        chatMessageContent.setText("What’s in this image?");
        chatMessageContent.setText("这张图片里有什么？请详细介绍每个物体结构、材质等等");
        chatMessageContents.add(chatMessageContent);

        chatMessageContent = new ChatMessageContent();
        chatMessageContent.setType("image_url");
        chatMessageContent.setImageUrl(new HashMap<String, Object>(){{
            put("url", "https://img.alicdn.com/imgextra/i2/O1CN01Vna2zP1wfwNaTVTec_!!6000000006336-0-tps-839-695.jpg");
//            put("url", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
//            put("url", "data:image/jpeg;base64," + ImageUtils.convertImageToBase64("https://img.alicdn.com/imgextra/i3/O1CN01r2mE2N25sbmZgm6uM_!!6000000007582-2-tps-256-256.png"));
        }});
        chatMessageContents.add(chatMessageContent);

        humanMessage.getAdditionalKwargs().put(CHAT_MESSAGE_CONTENTS_KEY, chatMessageContents);
        messages.add(humanMessage);

        BaseMessage response = llm.run(messages);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void test_gpt4v_multiple_image_inputs() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        List<BaseMessage> messages = new ArrayList<>();

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setAdditionalKwargs(new HashMap<>());

        List<ChatMessageContent> chatMessageContents = new ArrayList<>();
        ChatMessageContent chatMessageContent = new ChatMessageContent();
        chatMessageContent.setType("text");
//        chatMessageContent.setText("What are in these images? Is there any difference between them?");
        chatMessageContent.setText("这些图片中有什么？他们之间有什么区别吗？");
        chatMessageContents.add(chatMessageContent);

        chatMessageContent = new ChatMessageContent();
        chatMessageContent.setType("image_url");
        chatMessageContent.setImageUrl(new HashMap<String, Object>(){{
            put("url", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
        }});
        chatMessageContents.add(chatMessageContent);

        chatMessageContent = new ChatMessageContent();
        chatMessageContent.setType("image_url");
        chatMessageContent.setImageUrl(new HashMap<String, Object>(){{
//            put("url", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
            put("url", "data:image/jpeg;base64," + ImageUtils.convertImageToBase64("https://img.alicdn.com/imgextra/i3/O1CN01r2mE2N25sbmZgm6uM_!!6000000007582-2-tps-256-256.png"));
        }});
        chatMessageContents.add(chatMessageContent);

        humanMessage.getAdditionalKwargs().put(CHAT_MESSAGE_CONTENTS_KEY, chatMessageContents);
        messages.add(humanMessage);

        BaseMessage response = llm.run(messages);
        System.out.println("response:" + JSON.toJSONString(response));
    }

    @Test
    public void test_gpt4v_high_fidelity_image_understanding() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);

        List<BaseMessage> messages = new ArrayList<>();

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setAdditionalKwargs(new HashMap<>());

        List<ChatMessageContent> chatMessageContents = new ArrayList<>();
        ChatMessageContent chatMessageContent = new ChatMessageContent();
        chatMessageContent.setType("text");
//        chatMessageContent.setText("What’s in this image?");
        chatMessageContent.setText("这张图片里有什么？");
        chatMessageContents.add(chatMessageContent);

        chatMessageContent = new ChatMessageContent();
        chatMessageContent.setType("image_url");
        chatMessageContent.setImageUrl(new HashMap<String, Object>(){{
            put("url", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
            put("detail", "high");
        }});
        chatMessageContents.add(chatMessageContent);

        humanMessage.getAdditionalKwargs().put(CHAT_MESSAGE_CONTENTS_KEY, chatMessageContents);
        messages.add(humanMessage);

        BaseMessage response = llm.run(messages);
        System.out.println("response:" + JSON.toJSONString(response));
    }
}
