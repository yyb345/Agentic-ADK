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
package com.alibaba.langengine.core.tokenizers;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TokenizerTest {

    @Test
    public void test() {
        // success
        String text = "AI token is great!";
        List<Integer> result;
        String decodedText = null;

        QwenTokenizer qwenTokenizer = new QwenTokenizer();
        result = qwenTokenizer.encode(text);
        decodedText = qwenTokenizer.decode(result);
        System.out.println("qwenTokenizer encode:" + JSON.toJSONString(result) + ", decode:" + decodedText + ", tokenCount:" + qwenTokenizer.getTokenCount(text));

        GPT35AboveTokenizer gpt35AboveTokenizer = new GPT35AboveTokenizer();
        result = gpt35AboveTokenizer.encode(text);
        decodedText = gpt35AboveTokenizer.decode(result);
        System.out.println("gpt35AboveTokenizer encode:" + JSON.toJSONString(result) + ", decode:" + decodedText + ", tokenCount:" + gpt35AboveTokenizer.getTokenCount(text));

        GPT3Tokenizer gpt3Tokenizer = new GPT3Tokenizer();
        result = gpt3Tokenizer.encode(text);
        System.out.println("gpt3Tokenizer encode:" + JSON.toJSONString(result) + ", decode:" + decodedText + ", tokenCount:" + gpt3Tokenizer.getTokenCount(text));

        GPT2Tokenizer gpt2Tokenizer = new GPT2Tokenizer();
        result = gpt2Tokenizer.encode(text);
        System.out.println("gpt2Tokenizer encode:" + JSON.toJSONString(result) + ", decode:" + decodedText + ", tokenCount:" + gpt2Tokenizer.getTokenCount(text));
    }

    @Test
    public void test_2() {
        // success
        String text = "Hello，你是谁？";
        QwenTokenizer tokenizer = new QwenTokenizer();
        System.out.println(tokenizer.getTokenCount(text));
    }
}
