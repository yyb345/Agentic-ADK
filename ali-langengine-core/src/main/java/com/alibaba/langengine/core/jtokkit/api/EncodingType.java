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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum EncodingType {
    R50K_BASE("r50k_base"),
    P50K_BASE("p50k_base"),
    P50K_EDIT("p50k_edit"),
    CL100K_BASE("cl100k_base"),
    QWEN("qwen")
    ;

    private static final Map<String, EncodingType> nameToEncodingType = Arrays.stream(values())
            .collect(Collectors.toMap(EncodingType::getName, Function.identity()));

    private final String name;

    EncodingType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<EncodingType> fromName(final String name) {
        return Optional.ofNullable(nameToEncodingType.get(name));
    }
}
