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
package com.alibaba.langengine.huggingface;

import org.junit.jupiter.api.Test;

public class BertLangeCnnTest {

    @Test
    public void test_predict() {
        // success
        BartLargeCnn llm = new BartLargeCnn();
        long start = System.currentTimeMillis();
        System.out.println("response:" + llm.predict("The tower is 324 metres (1,063 ft) tall, about the same height as an 81-storey building, and the tallest structure in Paris. Its base is square, measuring 125 metres (410 ft) on each side. During its construction, the Eiffel Tower surpassed the Washington Monument to become the tallest man-made structure in the world, a title it held for 41 years until the Chrysler Building in New York City was finished in 1930. It was the first structure to reach a height of 300 metres. Due to the addition of a broadcasting aerial at the top of the tower in 1957, it is now taller than the Chrysler Building by 5.2 metres (17 ft). Excluding transmitters, the Eiffel Tower is the second tallest free-standing structure in France after the Millau Viaduct."));
        System.out.println("response:" + llm.predict("该塔高 324 米（1,063 英尺），与 81 层楼的高度大致相同，是巴黎最高的建筑。 它的底座是方形的，每边长 125 米（410 英尺）。 在建造期间，埃菲尔铁塔超越华盛顿纪念碑成为世界上最高的人造建筑，这一称号保持了 41 年，直到纽约市的克莱斯勒大厦于 1930 年竣工。它是第一个达到的建筑 300米的高度。 由于 1957 年在塔顶增加了广播天线，现在它比克莱斯勒大厦高 5.2 米（17 英尺）。 不包括发射器，埃菲尔铁塔是法国第二高的独立式建筑，仅次于米洛高架桥。"));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}
