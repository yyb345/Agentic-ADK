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

import java.util.Arrays;

public class BartLargeMnliTest {

    @Test
    public void test_predict() {
        // success
        BartLargeMnli llm = new BartLargeMnli();
        llm.setCandidateLabels(Arrays.asList(new String[] { "refund", "legal", "faq" }));
        long start = System.currentTimeMillis();
        System.out.println("response:" + llm.predict("Hi, I recently bought a device from your company but it is not working as advertised and I would like to get reimbursed!"));
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}
