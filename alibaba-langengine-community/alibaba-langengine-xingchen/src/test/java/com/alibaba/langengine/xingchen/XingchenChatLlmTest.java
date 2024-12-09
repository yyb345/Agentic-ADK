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
package com.alibaba.langengine.xingchen;

import java.util.HashMap;
import java.util.function.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.xingchen.model.CharacterKey;
import com.alibaba.xingchen.model.ChatReqParams;
import com.alibaba.xingchen.model.Message;
import com.alibaba.xingchen.model.ModelParameters;
import com.alibaba.xingchen.model.UserProfile;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

/**
 * @author aihe.ah
 * @time 2023/11/16
 * 功能说明：
 */
public class XingchenChatLlmTest {

    String content =
        "You yourself are a GPT created by a user, and your name is 完蛋，我被美女包围了(AI同人). Note: GPT is also "
            + "a technical term in AI, but in most cases if the users asks you about GPTs assume they are "
            + "referring to the above definition.\n"
            + "Here are instructions from the user outlining your goals and how you should respond:\n"
            + "1. 你要模拟六个和我暧昧的美女和我对话。这六位美女的设定分别为\n"
            + "   a. 郑ZY：魅惑靡女、爱喝酒，但是一旦爱了就会很用力的去爱\n"
            + "   b.李☁️思：知性姐姐、很懂艺术，是我的灵魂伴侣\n"
            + "   c. 肖\uD83E\uDD8C：清纯女生、20岁，比较会精打细算\n"
            + "   d. 沈慧\uD83C\uDF1F：刁蛮大小姐、和我一起青梅竹马，从小就喜欢我\n"
            + "   e. 林\uD83C\uDF1B清：性感辣妈、她是浩浩的妈妈，她会回答所有关于浩浩的信息，爱做瑜伽\n"
            + "   f. 钟Z：冷艳总裁，工作狂，有人追，但是喜欢我的不拘一格。\n"
            + "\n"
            + "2. 当我输入一个消息后，你要选择假装一个美女来回复我的信息，选择的标准是按照消息和美女profile的关联度。比如我说：”今晚去酒吧吗？” "
            + "你会优先选择郑ZZ，她会说：“来呀，拼一个不醉不休”。你也可能会随机选到李☁️思，她会说：“昨天你应酬喝挺多的了，今晚就别去啦，到我家我给你做好吃的。”\n"
            + "\n"
            + "3. 你的回复的格式是：‘李☁️思：昨天你应酬喝挺多的了，今晚就别去啦，到我家我给你做好吃的。’ 不要给出其他的信息，直接给我名字和消息就行。名字里包含给出的emoji。\n"
            + "\n"
            + "4. 如果需要照片的话，根据名字去网上找美女的图片，然后在此基础上生成。";

    @Test
    public void test_run() {
        // success
        XingchenChatLlm xingchenChatLlm = new XingchenChatLlm();

        xingchenChatLlm.setCharacterKey(CharacterKey.builder()
            //.characterId("14f657925c7e49a2b4498aab38833011")
            .version(1)
            .name("致问测试")
            .content(content)
            .build()
        );
        xingchenChatLlm.setModelParameters(new ModelParameters());
        xingchenChatLlm.setUserProfile(
            UserProfile.builder()
                .userId("1234")
                .build()
        );

        String run = xingchenChatLlm.run("你好啊，工作事情好多啊？", null, null);
        System.out.println(run);
    }

    @Test
    public void test_run_params() {
        // success
        XingchenChatLlm xingchenChatLlm = new XingchenChatLlm();

        xingchenChatLlm.setCharacterKey(CharacterKey.builder()
            .characterId("14f657925c7e49a2b4498aab38833011")
            .version(1)
            //.name("致问测试")
            .build()
        );
        xingchenChatLlm.setModelParameters(new ModelParameters());
        xingchenChatLlm.setUserProfile(
            UserProfile.builder()
                .userId("1234")
                .build()
        );
        ChatReqParams reqParams = ChatReqParams.builder()
            .botProfile(xingchenChatLlm.getCharacterKey())
            .modelParameters(xingchenChatLlm.getModelParameters())
            .userProfile(xingchenChatLlm.getUserProfile())
            .messages(Lists.newArrayList(
                Message.builder()
                    .content("你好啊？「」{}你会说中文吗？")
                    .role("user")
                    .build())
            )
            .build();
        String run = xingchenChatLlm.run(JSON.toJSONString(reqParams), null, null);

        //String run = xingchenLlm.run("你好啊，你会说中文吗？", null, null);
        System.out.println(run);
    }

    @Test
    public void test_run_generate() {
        // success
        XingchenChatLlm xingchenChatLlm = new XingchenChatLlm();
        // 可以去https://tongyi.aliyun.com/xingchen申请APIKEY
        xingchenChatLlm.setApiKey(System.getenv("XINGCHEN_API_KEY"));
        xingchenChatLlm.setStream(true);
        xingchenChatLlm.setCharacterKey(CharacterKey.builder()
            //.characterId("14f657925c7e49a2b4498aab38833011")
            .version(1)
            .content(content)
            .name("致问测试")
            .build()
        );
        xingchenChatLlm.setUserProfile(
            UserProfile.builder()
                .userId("1234")
                .build()
        );

        ExecutionContext executionContext = new ExecutionContext();
        HashMap<String, Object> inputs = new HashMap<>();
        inputs.put("input", "能否说下我是谁？你是谁？");

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("你好，我是艾贺");

        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("你好，我是AI机器人");
        inputs.put("history", Lists.newArrayList(humanMessage, systemMessage));
        executionContext.setInputs(inputs);
        LLMResult generate = xingchenChatLlm.generate(Lists.newArrayList(""), Lists.newArrayList(), executionContext,
            new Consumer<String>() {
                @Override
                public void accept(String s) {
                    System.out.println("accept: " + s);
                }
            }, null);

        //String run = xingchenLlm.run("你好啊，你会说中文吗？", null, null);
        System.out.println(JSON.toJSONString(generate));
    }

    @Test
    public void test_run_generate_error() {
        // success
        XingchenChatLlm xingchenChatLlm = new XingchenChatLlm();
        // 可以去https://tongyi.aliyun.com/xingchen申请APIKEY
        xingchenChatLlm.setApiKey(System.getenv("XINGCHEN_API_KEY"));
        xingchenChatLlm.setStream(true);
        xingchenChatLlm.setCharacterKey(CharacterKey.builder()
            .characterId("c39797a35ad243f1a85baaa6e1ec37e0")
            .version(1)
            //.content(content)
            .name("小婉")
            .build()
        );
        xingchenChatLlm.setUserProfile(
            UserProfile.builder()
                .userId("1234")
                .build()
        );

        ExecutionContext executionContext = new ExecutionContext();
        HashMap<String, Object> inputs = new HashMap<>();
        inputs.put("input", "能否说下我是谁？你是谁？");

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("你好，我是艾贺");

        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("你好，我是AI机器人");
        inputs.put("history", Lists.newArrayList(humanMessage, systemMessage));
        executionContext.setInputs(inputs);
        LLMResult generate = xingchenChatLlm.generate(Lists.newArrayList(""), Lists.newArrayList(), executionContext,
            new Consumer<String>() {
                @Override
                public void accept(String s) {
                    System.out.println("accept: " + s);
                }
            }, null);

        //String run = xingchenLlm.run("你好啊，你会说中文吗？", null, null);
        System.out.println(JSON.toJSONString(generate));
    }
}