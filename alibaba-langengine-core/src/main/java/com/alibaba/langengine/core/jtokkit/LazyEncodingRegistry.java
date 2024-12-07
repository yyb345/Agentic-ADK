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

import com.alibaba.langengine.core.jtokkit.api.Encoding;
import com.alibaba.langengine.core.jtokkit.api.EncodingType;
import com.alibaba.langengine.core.jtokkit.api.ModelType;

import java.util.Optional;

/**
 * {@link #getEncoding(EncodingType)} or {@link #getEncoding(String)} method is called.
 * When one of these methods is called, the requested {@link EncodingType} is registered.
 */
final class LazyEncodingRegistry extends AbstractEncodingRegistry {

    @Override
    public Encoding getEncoding(final EncodingType encodingType) {
        addEncoding(encodingType);
        return super.getEncoding(encodingType);
    }

    @Override
    public Optional<Encoding> getEncoding(final String encodingName) {
        EncodingType.fromName(encodingName).ifPresent(this::addEncoding);

        return super.getEncoding(encodingName);
    }

    @Override
    public Encoding getEncodingForModel(final ModelType modelType) {
        addEncoding(modelType.getEncodingType());
        return super.getEncodingForModel(modelType);
    }
}
