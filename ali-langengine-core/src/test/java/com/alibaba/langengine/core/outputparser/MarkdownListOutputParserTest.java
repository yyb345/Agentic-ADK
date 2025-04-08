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
package com.alibaba.langengine.core.outputparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author cuzz.lb
 * @date 2023/11/28 19:42
 */
public class MarkdownListOutputParserTest {
    MarkdownListOutputParser markdownListOutputParser = new MarkdownListOutputParser();

    @Test
    public void testGetFormatInstructions() throws Exception {
        // success
        String result = markdownListOutputParser.getFormatInstructions();
        System.out.println(result);
    }

    @Test
    public void testParse() throws Exception {
        // success
        List<String> result = markdownListOutputParser.parse("- foo\n" +
                "- bar\n" +
                "- baz");
        System.out.println(result);
        Assertions.assertEquals(Arrays.<String>asList("foo", "bar", "baz"), result);
    }

}

// Generated with love by TestMe :) Please report issues and submit feature requests at: https://weirddev.com/forum#!/testme