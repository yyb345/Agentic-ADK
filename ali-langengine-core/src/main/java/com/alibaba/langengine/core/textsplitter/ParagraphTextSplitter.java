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

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 段落分割
 *
 * @author xiaoxuan.lp
 */
@Data
public class ParagraphTextSplitter extends TextSplitter {

    private String separator = "\\R\\R";

    @Override
    public List<String> splitText(String text) {
        List<String> splits;
        if(!StringUtils.isEmpty(separator)) {
            splits = Arrays.asList(text.split(separator));
        } else {
            splits = Arrays.asList(new String[] { text });
        }
        return splits;
    }
}
