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
package com.alibaba.langengine.core.textsplitter.code;

import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import lombok.Data;

import java.util.Arrays;

/**
 * Attempts to split the text along Latex-formatted headings.
 *
 * @author xiaoxuan.lp
 */
@Data
public class LatexTextSplitter extends RecursiveCharacterTextSplitter {

    public LatexTextSplitter() {
        setSeparators(Arrays.asList(new String[] {
                "\n\\\\chapter\\{",
                "\n\\\\section\\{",
                "\n\\\\subsection\\{",
                "\n\\\\subsubsection\\{",
                "\n\\\\begin\\{enumerate\\}",
                "\n\\\\begin\\{itemize\\}",
                "\n\\\\begin\\{description\\}",
                "\n\\\\begin\\{list\\}",
                "\n\\\\begin\\{quote\\}",
                "\n\\\\begin\\{quotation\\}",
                "\n\\\\begin\\{verse\\}",
                "\n\\\\begin\\{verbatim\\}",
                "\n\\\\begin\\{align\\}",
                "\n",
                "$$",
                "$",
                " ",
                "",
        }));
    }
}
