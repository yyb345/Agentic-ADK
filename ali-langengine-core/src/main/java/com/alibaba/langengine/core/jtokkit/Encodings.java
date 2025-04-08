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

import com.alibaba.langengine.core.jtokkit.api.EncodingRegistry;

public final class Encodings {

    /**
     *
     * @return the new {@link EncodingRegistry}
     */
    public static EncodingRegistry newDefaultEncodingRegistry() {
        final DefaultEncodingRegistry registry = new DefaultEncodingRegistry();
        registry.initializeDefaultEncodings();
        return registry;
    }

    /**
     * loaded on-demand when they are first requested. For example, if you call
     * already loaded encoded.
     *
     * @return the new {@link EncodingRegistry}
     */
    public static EncodingRegistry newLazyEncodingRegistry() {
        return new LazyEncodingRegistry();
    }

    private Encodings() {
    }
}
