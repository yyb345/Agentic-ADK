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
package com.alibaba.langengine.model.gemini;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageConstant;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageContent;
import com.alibaba.langengine.core.util.ImageUtils;
import com.alibaba.langengine.gemini.model.GeminiChatModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeminiChatModelTest {

    @Test
    public void test_predict() {
        GeminiChatModel llm = new GeminiChatModel();
        System.out.println(llm.predict("你是谁？"));
    }
    @Test
    public void test_predict_gemini_pro_vision() {
        GeminiChatModel llm = new GeminiChatModel();
        llm.setModel("gemini-pro-vision");

        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        messages.add(humanMessage);

        Map<String, Object> additional = new HashMap<>();
        humanMessage.setAdditionalKwargs(additional);

        List<ChatMessageContent> chatMessageContents = new ArrayList<>();
        ChatMessageContent chatMessageContent = new ChatMessageContent();
        chatMessageContent.setType("text");
        chatMessageContent.setText("请问这张图片描述的是什么？");
        chatMessageContent.setImageUrl(new HashMap<String, Object>(){{
            put("mimeType", "image/jpeg");//支持image/png，image/jpeg，image/webp，image/heic，image/heif
            put("data", ImageUtils.convertImageToBase64("https://img.alicdn.com/imgextra/i3/O1CN01r2mE2N25sbmZgm6uM_!!6000000007582-2-tps-256-256.png"));
        }});
        chatMessageContents.add(chatMessageContent);
        additional.put(ChatMessageConstant.CHAT_MESSAGE_CONTENTS_KEY, chatMessageContents);

        BaseMessage response = llm.run(messages);
        System.out.println(JSON.toJSONString(response));
    }
}
