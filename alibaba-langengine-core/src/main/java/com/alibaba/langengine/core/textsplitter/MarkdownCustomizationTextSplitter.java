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

import com.alibaba.langengine.core.util.RegexUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yixuan
 * @date 2024/6/13 14:21
 */
public class MarkdownCustomizationTextSplitter extends TextSplitter {

    @Override
    public List<String> splitText(String text) {
        String[] sections = text.split(RegexUtils.MARKDOWN_CUSTOMIZATION_SPLIT_REGEX);
        List<String> splitSections = new ArrayList<>();

        for (String section : sections) {
            if (section.trim().isEmpty()) {
                continue;
            }

            // Check the length of the section
            if (section.length() > 200) {
                // Split by third-level headings
                String[] subSections = section.split(RegexUtils.MARKDOWN_CUSTOMIZATION_SPLIT_REGEX_THREE_LEVEL_SECTION);
                String combinedTitle = "";

                for (int i = 0; i < subSections.length; i++) {
                    if (i == 0) {
                        // This is the second-level title or the section's first part
                        combinedTitle = "## " + subSections[i].trim();
                        splitSections.add(combinedTitle);
                    } else {
                        // This is a third-level title or subsequent part
                        splitSections.add(combinedTitle + " ### " + subSections[i].trim());
                    }
                }
            } else {
                // If the section is not too long, add it as is
                splitSections.add("## " + section.trim());
            }
        }

        return splitSections;
    }
}
