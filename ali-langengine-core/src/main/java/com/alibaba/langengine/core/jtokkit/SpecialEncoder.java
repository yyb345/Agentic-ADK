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
package com.alibaba.langengine.core.jtokkit;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.HashMap;
import java.util.Map;

final class SpecialEncoder {
    private static final String SPECIAL_START = "<|";
    private static final String SPECIAL_END = "|>";
    private final Map<Integer, String> encodedToDecoded;

    SpecialEncoder(Map<String, Integer> encoder) {
        this.encodedToDecoded = new HashMap<>(encoder.size());
        for (Map.Entry<String, Integer> entry : encoder.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            assert key.contains(SPECIAL_START) && key.contains(SPECIAL_END) : "Special tokens must contain <| and |> (but was " + key + ")";

            encodedToDecoded.put(value, key);
        }
    }

    byte[] decodeIfPresent(Integer encodedToken) {
        String result = encodedToDecoded.get(encodedToken);
        return result != null ? result.getBytes(UTF_8) : null;
    }

    void checkForSpecialTokens(String text) {
        if (text.contains(SPECIAL_START) && text.contains(SPECIAL_END)) {
            for (String specialToken : encodedToDecoded.values()) {
                if (text.contains(specialToken)) {
                    throw new UnsupportedOperationException("Encoding special tokens is not supported.");
                }
            }
        }
    }
}
