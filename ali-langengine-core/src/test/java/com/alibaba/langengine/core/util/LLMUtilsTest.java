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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;


public class LLMUtilsTest {

    @Test
    public void testEnforceStopTokens() {
        // success
        String realAnswer = "Answer is here. ";
        List<String> stops = Arrays.asList("Human:", "AI:", "\nObservation:");
        {
            String responseContent = realAnswer;
            responseContent = LLMUtils.enforceStopTokens(responseContent, stops);
            Assertions.assertEquals(realAnswer, responseContent);
        }
        {
            String responseContent = realAnswer + "Human: I am human. " + "AI: I am AI. " + "\nObservation: I am observation.";
            responseContent = LLMUtils.enforceStopTokens(responseContent, stops);
            Assertions.assertEquals(realAnswer, responseContent);
        }
        {
            String responseContent = realAnswer + "AI: I am AI. " + "Human: I am human. " + "\nObservation: I am observation.";
            responseContent = LLMUtils.enforceStopTokens(responseContent, stops);
            Assertions.assertEquals(realAnswer, responseContent);
        }
        {
            String responseContent = realAnswer + "\nObservation: I am observation." + "AI: I am AI. " + "Human: I am human. ";
            responseContent = responseContent.split(String.join("|", stops), 2)[0];
            Assertions.assertEquals(realAnswer, responseContent);
        }
    }
}
