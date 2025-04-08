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
package com.alibaba.langengine.core.chain.llmguard.input;

import com.alibaba.langengine.core.chain.llmguard.ScannerResult;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Sinh Le
 */
class BanSubstrings implements InputScanner {
    private final List<String> banSubstrings;
    private final boolean isCaseSensitive;
    private final boolean machTypeWord;

    private final boolean containAll;

    private List<Pattern> patterns;

    BanSubstrings(List<String> banSubstrings, boolean isCaseSensitive, boolean machTypeWord, boolean containAll) {
        this.isCaseSensitive = isCaseSensitive;
        this.machTypeWord = machTypeWord;
        this.containAll = containAll;
        if (isCaseSensitive) {
            this.banSubstrings = banSubstrings.stream().map(String::toLowerCase).collect(Collectors.toList());
        } else {
            this.banSubstrings = banSubstrings;
        }
        if (machTypeWord) {
            patterns = banSubstrings.stream().map(it -> Pattern.compile("\\b" + it + "\\b")).collect(Collectors.toList());
        }
    }

    @Override
    public ScannerResult scan(String text, Map<String, Object> extraAttributes) {
        String needToValidateText = isCaseSensitive ? text.toLowerCase() : text;
        boolean result;
        if (machTypeWord) {
            if (containAll) {
                result = patterns.stream().allMatch(it -> it.matcher(needToValidateText).find());
            } else {
                result = patterns.stream().anyMatch(it -> it.matcher(needToValidateText).find());
            }
        } else {
            if (containAll) {
                result = banSubstrings.stream().allMatch(needToValidateText::contains);
            } else {
                result = banSubstrings.stream().anyMatch(needToValidateText::contains);
            }
        }
        return new ScannerResult(text, !result, result ? 0.0 : 1.0);
    }

    @Override
    public String getName() {
        return "BanSubstrings";
    }
}
