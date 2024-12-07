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
package com.alibaba.langengine.gpt.nl2api;

import com.alibaba.fastjson.JSON;

import org.junit.jupiter.api.Test;

public class TextRequestsWrapperTest {

    @Test
    public void testGet() {
        // success
        TextRequestsWrapper textRequestsWrapper = new TextRequestsWrapper(null);
        String response = textRequestsWrapper.get("http://localhost:7001/countryInfo/capital/france");
        System.out.println(JSON.toJSONString(response));
    }
}
