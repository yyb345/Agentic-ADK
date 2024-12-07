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

/**
 * The result of encoding operation.
 */
public final class EncodingResult {
    private final IntArrayList tokens;
    private final boolean truncated;

    public EncodingResult(final IntArrayList tokens, final boolean truncated) {
        this.tokens = tokens;
        this.truncated = truncated;
    }

    /**
     * Returns the list of token ids
     *
     * @return the list of token ids
     */
    public IntArrayList getTokens() {
        return tokens;
    }

    /**
     * Returns true if the token list was truncated because the maximum token length was exceeded
     *
     * @return true if the token list was truncated because the maximum token length was exceeded
     */
    public boolean isTruncated() {
        return truncated;
    }

    @Override
    public String toString() {
        return "EncodingResult{"
                + "tokens=" + tokens
                + ", truncated=" + truncated
                + '}';
    }
}