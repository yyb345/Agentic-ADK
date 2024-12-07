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
package com.alibaba.langengine.core.chain.qageneration;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sinh Le
 */
public class QAGenerationChainTest {

    @Test
    public void testParse() {
        QAGenerationChain.processLlmResult("diverse array of businesses around the world in numerous sectors.\n" +
                "\n" +
                "{\n" +
                "    \"question\": \"What is Alibaba?\",\n" +
                "    \"answer\": \"Alibaba is a Chinese multinational technology company specializing in e-commerce, retail, Internet, and technology.\"\n" +
                "}");
    }

    @Test
    public void test_run_simple_qa() {
        // success
        ChatOpenAI llm = new ChatOpenAI();
        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        llm.setTemperature(0d);
        llm.setMaxTokens(1024);

        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
        textSplitter.setMaxChunkOverlap(100);
        textSplitter.setMaxChunkSize(300);

        QAGenerationChain qaGenerationChain = QAGenerationChain.init(llm, null);
        qaGenerationChain.setTextSplitter(textSplitter);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("text", "Alibaba Group Holding Limited, or Alibaba (Chinese: 阿里巴巴), is a Chinese multinational technology company specializing in e-commerce, retail, Internet, and technology. Founded on 28 June 1999[1] in Hangzhou, Zhejiang, the company provides consumer-to-consumer (C2C), business-to-consumer (B2C), and business-to-business (B2B) sales services via Chinese and global marketplaces, as well as local consumer, digital media and entertainment, logistics and cloud computing services. It owns and operates a diverse portfolio of companies around the world in numerous business sectors.\n" +
                "\n" +
                "On 19 September 2014, Alibaba's initial public offering (IPO) on the New York Stock Exchange raised US$25 billion, giving the company a market value of US$231 billion and, by far, then the largest IPO in world history.[8] It is one of the top 10 most valuable corporations,[9] and is named the 31st-largest public company in the world on the Forbes Global 2000 2020 list.[10] In January 2018, Alibaba became the second Asian company to break the US$500 billion valuation mark, after its competitor Tencent.[11] As of 2022, Alibaba has the ninth-highest global brand valuation.[12]\n" +
                "\n" +
                "Alibaba is one of the world's largest retailers and e-commerce companies. In 2020, it was also rated as the fifth-largest artificial intelligence company.[13] It is also one of the biggest venture capital firms and investment corporations in the world, as well as the second largest financial services group behind Visa via its fintech arm Ant Group. The company hosts the largest B2B (Alibaba.com), C2C (Taobao), and B2C (Tmall) marketplaces in the world.[14] It has been expanding into the media industry, with revenues rising by triple percentage points year after year.[15] It also set the record on the 2018 edition of China's Singles' Day, the world's biggest online and offline shopping day.[16]\n" +
                "\n");
        Map<String, Object> result = qaGenerationChain.call(inputs);
        System.out.println(JSON.toJSONString(result));
    }
}
