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
package com.alibaba.langengine.core.textsplitter.py;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.TextSplitter;
import com.alibaba.langengine.core.util.PythonUtils;
import lombok.Data;

import java.util.List;

/**
 * Implementation of splitting text that looks at tokens.
 *
 * @author xiaoxuan.lp
 */
@Data
public class TokenTextSplitter extends TextSplitter {

    private String encodingName = "gpt2";
    @Override
    public List<String> splitText(String text) {
        String result = PythonUtils.invokeMethodAsResource(getClass(), "tokenTextSplitter_splitText.py", text);
        List<String> splits = JSON.parseArray(result, String.class);
        return splits;
    }
}
