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

import com.alibaba.langengine.core.jtokkit.api.*;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractEncodingRegistry implements EncodingRegistry {
    private final ConcurrentHashMap<String, Encoding> encodings = new ConcurrentHashMap<>();

    @Override
    public Optional<Encoding> getEncoding(final String encodingName) {
        return Optional.ofNullable(encodings.get(encodingName));
    }

    @Override
    public Encoding getEncoding(final EncodingType encodingType) {
        return Objects.requireNonNull(
                encodings.get(encodingType.getName()),
                () -> "No encoding registered for encoding type " + encodingType.getName()
        );
    }

    @Override
    public Optional<Encoding> getEncodingForModel(final String modelName) {
        final Optional<ModelType> modelType = ModelType.fromName(modelName);
        if (modelType.isPresent()) {
            return Optional.of(getEncodingForModel(modelType.get()));
        }

        if (modelName.startsWith(ModelType.GPT_4_32K.getName())) {
            return Optional.of(getEncodingForModel(ModelType.GPT_4_32K));
        }

        if (modelName.startsWith(ModelType.GPT_4.getName())) {
            return Optional.of(getEncodingForModel(ModelType.GPT_4));
        }

        if (modelName.startsWith(ModelType.GPT_3_5_TURBO_16K.getName())) {
            return Optional.of(getEncodingForModel(ModelType.GPT_3_5_TURBO_16K));
        }

        if (modelName.startsWith(ModelType.GPT_3_5_TURBO.getName())) {
            return Optional.of(getEncodingForModel(ModelType.GPT_3_5_TURBO));
        }

        return Optional.empty();
    }

    @Override
    public Encoding getEncodingForModel(final ModelType modelType) {
        return Objects.requireNonNull(
                encodings.get(modelType.getEncodingType().getName()),
                () -> "No encoding registered for model type " + modelType.getName()
        );
    }

    @Override
    public EncodingRegistry registerGptBytePairEncoding(final GptBytePairEncodingParams parameters) {
        return registerCustomEncoding(EncodingFactory.fromParameters(parameters));
    }

    @Override
    public EncodingRegistry registerCustomEncoding(final Encoding encoding) {
        final String encodingName = encoding.getName();
        final Encoding previousEncoding = encodings.putIfAbsent(encodingName, encoding);
        if (previousEncoding != null) {
            throw new IllegalStateException("Encoding " + encodingName + " already registered");
        }

        return this;
    }

    protected final void addEncoding(final EncodingType encodingType) {
        switch (encodingType) {
            case R50K_BASE:
                encodings.computeIfAbsent(encodingType.getName(), k -> EncodingFactory.r50kBase());
                break;
            case P50K_BASE:
                encodings.computeIfAbsent(encodingType.getName(), k -> EncodingFactory.p50kBase());
                break;
            case P50K_EDIT:
                encodings.computeIfAbsent(encodingType.getName(), k -> EncodingFactory.p50kEdit());
                break;
            case CL100K_BASE:
                encodings.computeIfAbsent(encodingType.getName(), k -> EncodingFactory.cl100kBase());
                break;
            case QWEN:
                encodings.computeIfAbsent(encodingType.getName(), k -> EncodingFactory.qwen());
                break;
            default:
                throw new IllegalStateException("Unknown encoding type " + encodingType.getName());
        }
    }
}
