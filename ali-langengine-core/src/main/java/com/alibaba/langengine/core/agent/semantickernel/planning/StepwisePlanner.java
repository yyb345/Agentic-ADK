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
package com.alibaba.langengine.core.agent.semantickernel.planning;

import com.alibaba.langengine.core.agent.AgentOutputParser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.alibaba.langengine.core.agent.semantickernel.PromptConstants.STEPWISE_PLANNER_PROMPT_TEMPLATE;

/**
 * ReACT实现的Planner
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class StepwisePlanner extends BasePlanner {

    public StepwisePlanner() {
        this(false);
    }

    public StepwisePlanner(boolean isCH) {
        setPromptTemplate(isCH ? STEPWISE_PLANNER_PROMPT_TEMPLATE : STEPWISE_PLANNER_PROMPT_TEMPLATE);
    }

    @Override
    public AgentOutputParser getPlannerOutputParser() {
        StepwisePlannerOutputParser outputParser = new StepwisePlannerOutputParser();
        outputParser.setToolMap(getToolMap());
        return outputParser;
    }

//    public static void main(String[] args) {
//        String text = "[FINAL ANSWER]\n" +
//                "Here are some date ideas for your girlfriend's birthday, translated into Chinese:\n" +
//                "\n" +
//                "1. 在公园野餐 (Go for a picnic in the park).\n" +
//                "2. 一起参加烹饪课程 (Take a cooking class together).\n" +
//                "3. 探索当地的博物馆或艺术画廊 (Explore a local museum or art gallery).\n" +
//                "4. 在家里看电影马拉松 (Have a movie marathon at home).\n" +
//                "5. 去远足或自然步行 (Go for a hike or nature walk).\n" +
//                "6. 参加现场音乐或喜剧表演 (Attend a live music or comedy show).\n" +
//                "7. 与棋盘游戏或纸牌游戏一起度过游戏之夜 (Have a game night with board games or card games).\n" +
//                "8. 进行一次风景如画的驾车旅行，并停留在有趣的地标 (Take a scenic drive and stop at interesting landmarks).\n" +
//                "9. 在家里DIY水疗之夜，使用面膜和按摩 (Have a DIY spa night at home with facemasks and massages).\n" +
//                "10. 一起去舞蹈课 (Take a dance class together).\n" +
//                "\n" +
//                "Hope you have a great celebration!";
//        Matcher funishMatcher = S_FINAL_REGEX.matcher(text);
//        if(funishMatcher.find()) {
//            String finishAnswer = funishMatcher.group(1).trim();
//            System.out.println("finishAnswer:" + finishAnswer);
//        }
//    }
}
