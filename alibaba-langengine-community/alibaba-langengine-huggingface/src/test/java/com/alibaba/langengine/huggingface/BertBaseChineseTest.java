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

public class BertBaseChineseTest {

    @Test
    public void test_predict() {
        // success
        BertBaseChinese llm = new BertBaseChinese();
        long start = System.currentTimeMillis();
        System.out.println("response:" + llm.predict("巴黎是[MASK]国的首都。"));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}
