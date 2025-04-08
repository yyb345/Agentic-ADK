/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.mcp.spec;

import lombok.Getter;

import java.util.List;

/**
 * The server's preferences for model selection, requested by the client during sampling.
 */
@Getter
public class ModelPreferences {

    private List<ModelHint> hints;
    private Double costPriority;
    private Double speedPriority;
    private Double intelligencePriority;

    public ModelPreferences(List<ModelHint> hints, Double costPriority, Double speedPriority, Double intelligencePriority) {
        if (costPriority != null && (costPriority < 0.0 || costPriority > 1.0)) {
            throw new IllegalArgumentException("costPriority must be in 0.0 <= x <= 1.0 value range");
        }
        if (speedPriority != null && (speedPriority < 0.0 || speedPriority > 1.0)) {
            throw new IllegalArgumentException("speedPriority must be in 0.0 <= x <= 1.0 value range");
        }
        if (intelligencePriority != null && (intelligencePriority < 0.0 || intelligencePriority > 1.0)) {
            throw new IllegalArgumentException("intelligencePriority must be in 0.0 <= x <= 1.0 value range");
        }

        this.hints = hints;
        this.costPriority = costPriority;
        this.speedPriority = speedPriority;
        this.intelligencePriority = intelligencePriority;
    }
}
