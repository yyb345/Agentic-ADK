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
package com.alibaba.langengine.dashscope;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import com.alibaba.langengine.core.tokenizers.Tokenizer;
import com.alibaba.langengine.dashscope.tokenizers.DashScopeTokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DashScopeTokenizerTest {

    @Test
    public void test() {
        // success
        String text = "Who are you? I'am sunleepy.";
        Tokenizer tokenizer = new DashScopeTokenizer(DashScopeModelName.QWEN_TURBO);
        Integer tokenCount = tokenizer.getTokenCount(text);
        System.out.println(tokenCount);
    }

    @Test
    public void test_TextSplitter() {
        // success
        String text = "Who are you? I'am sunleepy.";
        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
        textSplitter.setMaxChunkSize(10);
        textSplitter.setMaxChunkOverlap(0);
        textSplitter.setTokenizer(new DashScopeTokenizer(DashScopeModelName.QWEN_TURBO));

        List<Document> documents = textSplitter.createDocuments(text);
        System.out.println(JSON.toJSONString(documents));
    }
}
