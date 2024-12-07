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
package com.alibaba.langengine.minimax.model;

import java.util.function.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.BizExecutionContext;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.minimax.model.model.Minimax55Parameters;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

/**
 * @author aihe.ah
 * @time 2024/1/2
 * 功能说明：
 * 测试下Minimax 55的模型效果
 */
public class MiniMax55LlmTest {

    @Test
    public void run() {
        // success
        //miniMaxLlm.setApiKey(System.getenv("MINIMAX_API_KEY"));
        //miniMaxLlm.setGroupId(System.getenv("MINIMAX_GROUP_ID"));
        MiniMax55Llm miniMax55Llm = new MiniMax55Llm(
//            System.getenv("MINIMAX_GROUP_ID"),
//            System.getenv("MINIMAX_API_KEY")
        );
        //Minimax55Parameters miniMaxParameters = new Minimax55Parameters();
        Minimax55Parameters parameters = JSON.parseObject("{\n"
            + "    \"model\": \"abab5-chat\",\n"
            + "    \"prompt\": \"你是一个擅长发现故事中蕴含道理的专家，你很善于基于我给定的故事发现其中蕴含的道理。\",\n"
            + "    \"role_meta\": {\n"
            + "        \"user_name\": \"我\",\n"
            + "        \"bot_name\": \"专家\"\n"
            + "    },\n"
            + "    \"messages\": [\n"
            + "        {\n"
            + "            \"sender_type\": \"USER\",\n"
            + "            \"text\": "
            +
            "\"我给定的故事：从前，在森林里有只叫聪聪的小猪，他既勤劳，又乐于助人，小动物们都很喜欢他。有一次，小兔子放风筝不小心将风筝挂在了树上，那是小兔子最喜欢的东西呀!他“呜呜”地哭了起来。这时，正巧聪聪路过。他见了，连忙问:“怎么了? 你怎么哭了呀?”“我，我的风筝被挂在树上了。”小兔子抽噎着说。聪聪听了，不假思索地说:“你先回去吧，放心，我一定帮你。”“真的吗?太好了!”小兔子高兴地回家去了。聪聪尝试了几次，都没能把风筝摘下来，这可把他愁坏了。聪聪想了又想，突然灵机一动，想到一个好办法。他委托小猴弄到风筝线，又找到风筝纸，他要给小兔子重新做一个风筝。风筝做好了，聪聪将它送给了小兔子，小兔子十分感动，聪聪却说:“这是应该的。聪聪的生日到了，可他全心全意地为小动物们解决问题，连自己的生日都忘记了。小动物们商量着，给聪聪过一个生日，可送什么礼物好呢?小动物们思索着。“有了!”小猴子说，“聪聪的愿望就是像一只美丽的蝴蝶，在天空飞翔。我们可以吹一个大大的泡泡，让小猪站在里面，就可以飞了!”“对呀!”“太好了!”动物们高兴极了，七嘴八舌地议论起来。聪聪的生日到了，他忙了一天，推开家门准备休息。可一推开门，小动物就拥了上去:“生日快乐!”聪聪反应过来了，高兴地说:“谢谢，谢谢!”小猴子说:“我们还有礼物给你呢!”说着，几个小动物吹出一个大大的泡泡，罩住聪聪，能飞起来吗? 小动物们屏气凝神地看着。慢慢地，泡泡一点点升起，带着聪聪飞了起来!聪聪激动得热泪盈眶，大声喊着:“我飞起来了!我飞起来了!”泡泡掉了，聪聪却在天上自由地飞翔，聪聪真的变成了一只美丽的蝴蝶!请你仔细阅读我给定的故事，然后给出蕴含的道理，道理控制在100字以内。\"\n"
            + "        }\n"
            + "    ],\n"
            + "    \"temperature\": 0.5\n"
            + "}", Minimax55Parameters.class);
        parameters.setStream(true);
        //parameters.setUseStandardSse(true);

        miniMax55Llm.setMiniMaxParameters(parameters);
        String run = miniMax55Llm.run(
            "我给定的故事：从前，在森林里有只叫聪聪的小猪，他既勤劳，又乐于助人，小动物们都很喜欢他。有一次，小兔子放风筝不小心将风筝挂在了树上，那是小兔子最喜欢的东西呀!他“呜呜”地哭了起来。这时，正巧聪聪路过。他见了，连忙问:“怎么了? 你怎么哭了呀?”“我，我的风筝被挂在树上了。”小兔子抽噎着说。聪聪听了，不假思索地说:“你先回去吧，放心，我一定帮你。”“真的吗?太好了!”小兔子高兴地回家去了。聪聪尝试了几次，都没能把风筝摘下来，这可把他愁坏了。聪聪想了又想，突然灵机一动，想到一个好办法。他委托小猴弄到风筝线，又找到风筝纸，他要给小兔子重新做一个风筝。风筝做好了，聪聪将它送给了小兔子，小兔子十分感动，聪聪却说:“这是应该的。聪聪的生日到了，可他全心全意地为小动物们解决问题，连自己的生日都忘记了。小动物们商量着，给聪聪过一个生日，可送什么礼物好呢?小动物们思索着。“有了!”小猴子说，“聪聪的愿望就是像一只美丽的蝴蝶，在天空飞翔。我们可以吹一个大大的泡泡，让小猪站在里面，就可以飞了!”“对呀!”“太好了!”动物们高兴极了，七嘴八舌地议论起来。聪聪的生日到了，他忙了一天，推开家门准备休息。可一推开门，小动物就拥了上去:“生日快乐!”聪聪反应过来了，高兴地说:“谢谢，谢谢!”小猴子说:“我们还有礼物给你呢!”说着，几个小动物吹出一个大大的泡泡，罩住聪聪，能飞起来吗? 小动物们屏气凝神地看着。慢慢地，泡泡一点点升起，带着聪聪飞了起来!聪聪激动得热泪盈眶，大声喊着:“我飞起来了!我飞起来了!”泡泡掉了，聪聪却在天上自由地飞翔，聪聪真的变成了一只美丽的蝴蝶!请你仔细阅读我给定的故事，然后给出蕴含的道理，道理控制在500字以内。",
            null,
                null
        );
        System.out.println(run);
    }

    @Test
    public void generate() {
        // success
        //miniMaxLlm.setApiKey(System.getenv("MINIMAX_API_KEY"));
        //miniMaxLlm.setGroupId(System.getenv("MINIMAX_GROUP_ID"));
        MiniMax55Llm miniMax55Llm = new MiniMax55Llm(
            System.getenv("MINIMAX_GROUP_ID"),
            System.getenv("MINIMAX_API_KEY")
        );
        //Minimax55Parameters miniMaxParameters = new Minimax55Parameters();
        Minimax55Parameters parameters = JSON.parseObject("{\n"
            + "    \"model\": \"abab5-chat\",\n"
            + "    \"prompt\": \"你是一个擅长发现故事中蕴含道理的专家，你很善于基于我给定的故事发现其中蕴含的道理。\",\n"
            + "    \"role_meta\": {\n"
            + "        \"user_name\": \"我\",\n"
            + "        \"bot_name\": \"专家\"\n"
            + "    },\n"
            + "    \"messages\": [\n"
            + "        {\n"
            + "            \"sender_type\": \"USER\",\n"
            + "            \"text\": "
            +
            "\"我给定的故事：从前，在森林里有只叫聪聪的小猪，他既勤劳，又乐于助人，小动物们都很喜欢他。有一次，小兔子放风筝不小心将风筝挂在了树上，那是小兔子最喜欢的东西呀!他“呜呜”地哭了起来。这时，正巧聪聪路过。他见了，连忙问:“怎么了? 你怎么哭了呀?”“我，我的风筝被挂在树上了。”小兔子抽噎着说。聪聪听了，不假思索地说:“你先回去吧，放心，我一定帮你。”“真的吗?太好了!”小兔子高兴地回家去了。聪聪尝试了几次，都没能把风筝摘下来，这可把他愁坏了。聪聪想了又想，突然灵机一动，想到一个好办法。他委托小猴弄到风筝线，又找到风筝纸，他要给小兔子重新做一个风筝。风筝做好了，聪聪将它送给了小兔子，小兔子十分感动，聪聪却说:“这是应该的。聪聪的生日到了，可他全心全意地为小动物们解决问题，连自己的生日都忘记了。小动物们商量着，给聪聪过一个生日，可送什么礼物好呢?小动物们思索着。“有了!”小猴子说，“聪聪的愿望就是像一只美丽的蝴蝶，在天空飞翔。我们可以吹一个大大的泡泡，让小猪站在里面，就可以飞了!”“对呀!”“太好了!”动物们高兴极了，七嘴八舌地议论起来。聪聪的生日到了，他忙了一天，推开家门准备休息。可一推开门，小动物就拥了上去:“生日快乐!”聪聪反应过来了，高兴地说:“谢谢，谢谢!”小猴子说:“我们还有礼物给你呢!”说着，几个小动物吹出一个大大的泡泡，罩住聪聪，能飞起来吗? 小动物们屏气凝神地看着。慢慢地，泡泡一点点升起，带着聪聪飞了起来!聪聪激动得热泪盈眶，大声喊着:“我飞起来了!我飞起来了!”泡泡掉了，聪聪却在天上自由地飞翔，聪聪真的变成了一只美丽的蝴蝶!请你仔细阅读我给定的故事，然后给出蕴含的道理，道理控制在100字以内。\"\n"
            + "        }\n"
            + "    ],\n"
            + "    \"temperature\": 0.5\n"
            + "}", Minimax55Parameters.class);
        parameters.setStream(true);
        //parameters.setUseStandardSse(true);

        miniMax55Llm.setMiniMaxParameters(parameters);
        LLMResult llmResult = miniMax55Llm.generate(
            Lists.newArrayList(
                "我给定的故事：从前，在森林里有只叫聪聪的小猪，他既勤劳，又乐于助人，小动物们都很喜欢他。有一次，小兔子放风筝不小心将风筝挂在了树上，那是小兔子最喜欢的东西呀!他“呜呜”地哭了起来。这时，正巧聪聪路过。他见了，连忙问:“怎么了? 你怎么哭了呀?”“我，我的风筝被挂在树上了。”小兔子抽噎着说。聪聪听了，不假思索地说:“你先回去吧，放心，我一定帮你。”“真的吗?太好了!”小兔子高兴地回家去了。聪聪尝试了几次，都没能把风筝摘下来，这可把他愁坏了。聪聪想了又想，突然灵机一动，想到一个好办法。他委托小猴弄到风筝线，又找到风筝纸，他要给小兔子重新做一个风筝。风筝做好了，聪聪将它送给了小兔子，小兔子十分感动，聪聪却说:“这是应该的。聪聪的生日到了，可他全心全意地为小动物们解决问题，连自己的生日都忘记了。小动物们商量着，给聪聪过一个生日，可送什么礼物好呢?小动物们思索着。“有了!”小猴子说，“聪聪的愿望就是像一只美丽的蝴蝶，在天空飞翔。我们可以吹一个大大的泡泡，让小猪站在里面，就可以飞了!”“对呀!”“太好了!”动物们高兴极了，七嘴八舌地议论起来。聪聪的生日到了，他忙了一天，推开家门准备休息。可一推开门，小动物就拥了上去:“生日快乐!”聪聪反应过来了，高兴地说:“谢谢，谢谢!”小猴子说:“我们还有礼物给你呢!”说着，几个小动物吹出一个大大的泡泡，罩住聪聪，能飞起来吗? 小动物们屏气凝神地看着。慢慢地，泡泡一点点升起，带着聪聪飞了起来!聪聪激动得热泪盈眶，大声喊着:“我飞起来了!我飞起来了!”泡泡掉了，聪聪却在天上自由地飞翔，聪聪真的变成了一只美丽的蝴蝶!请你仔细阅读我给定的故事，然后给出蕴含的道理，道理控制在500字以内。"),
            null
            , new BizExecutionContext()
            , new Consumer<String>() {
                @Override
                public void accept(String s) {
                    System.out.println("accept:" + s);
                }
            }, null
        );
        System.out.println(JSON.toJSONString(llmResult));
    }
}