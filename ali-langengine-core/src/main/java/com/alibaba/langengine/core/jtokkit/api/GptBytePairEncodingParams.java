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
package com.alibaba.langengine.core.jtokkit.api;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Parameter for the byte pair encoding used to tokenize for the OpenAI GPT models.
 * <p>
 *     This library supports the encodings that are listed in {@link EncodingType} out of the box.
 *     But if you want to use a custom encoding, you can use this class to pass the parameters to the library.
 *     Use {@link EncodingRegistry#registerGptBytePairEncoding(GptBytePairEncodingParams)} to register your custom encoding
 *     to the registry, so that you can easily use your encoding in conjunction with the predefined ones.
 * <p>
 *     The encoding parameters are:
 *     <ul>
 *         <li>name: The name of the encoding. This is used to identify the encoding and must be unique.</li>
 *         <li>pattern: The pattern that is used to split the input text into tokens.</li>
 *         <li>encoder: The encoder that maps the tokens to their ids.</li>
 *         <li>specialTokensEncoder: The encoder that maps the special tokens to their ids.</li>
 *     </ul>
 */
public final class GptBytePairEncodingParams {
    private final String name;
    private final Pattern pattern;
    private final Map<byte[], Integer> encoder;
    private final Map<String, Integer> specialTokensEncoder;

    /**
     * Creates a new instance of {@link GptBytePairEncodingParams}.
     *
     * @param name the name of the encoding. This is used to identify the encoding and must be unique
     * @param pattern the pattern that is used to split the input text into tokens.
     * @param encoder the encoder that maps the tokens to their ids
     * @param specialTokensEncoder the encoder that maps the special tokens to their ids
     */
    public GptBytePairEncodingParams(
            final String name,
            final Pattern pattern,
            final Map<byte[], Integer> encoder,
            final Map<String, Integer> specialTokensEncoder
    ) {
        this.name = name;
        this.pattern = pattern;
        this.encoder = encoder;
        this.specialTokensEncoder = specialTokensEncoder;
    }

    public String getName() {
        return name;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Map<byte[], Integer> getEncoder() {
        return encoder;
    }

    public Map<String, Integer> getSpecialTokensEncoder() {
        return specialTokensEncoder;
    }
}
