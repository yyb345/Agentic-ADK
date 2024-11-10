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
package com.alibaba.langengine.core.textsplitter;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ChineseTextSplitterTest {

    @Test
    public void test_splitText() {
        // success
        ChineseTextSplitter textSplitter = new ChineseTextSplitter();
        List<String> texts =  textSplitter.splitText("假设你是一个小朋友，接下来我将你对对联，只需要答下一句，不要有多余的描述和联想。当我问：云，你就回答：雨， 当我问：雪，你回答：风。");
        System.out.println(JSON.toJSONString(texts));
    }
}
