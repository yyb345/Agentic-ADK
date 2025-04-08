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
package com.alibaba.langengine.core.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author aihe.ah
 * @time 2023/10/11
 * 功能说明：
 */
public class WorkPropertiesUtilsTest {

    @Test
    public void text_get() {
        // success
        String notExist = WorkPropertiesUtils.get("notExist");
        String retrievalQaRecommendCount = WorkPropertiesUtils.get("retrieval_qa_recommend_count");
        String openaiApiKey = WorkPropertiesUtils.getFirstAvailable("OPENAI_API_KEY");
        Assertions.assertTrue(StringUtils.isNotEmpty(retrievalQaRecommendCount));
        Assertions.assertTrue(StringUtils.isEmpty(notExist));
        Assertions.assertTrue(StringUtils.isNotEmpty(openaiApiKey));
    }
}