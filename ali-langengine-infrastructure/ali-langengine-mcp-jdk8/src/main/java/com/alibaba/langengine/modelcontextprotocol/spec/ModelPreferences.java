/*
 * Copyright 2025 Alibaba Group Holding Ltd.
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
package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model preferences for sampling.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ModelPreferences {
    private final List<ModelHint> hints;
    private final Double costPriority;
    private final Double speedPriority;
    private final Double intelligencePriority;

    public ModelPreferences(
            @JsonProperty("hints") List<ModelHint> hints,
            @JsonProperty("costPriority") Double costPriority,
            @JsonProperty("speedPriority") Double speedPriority,
            @JsonProperty("intelligencePriority") Double intelligencePriority) {
        this.hints = hints;
        this.costPriority = costPriority;
        this.speedPriority = speedPriority;
        this.intelligencePriority = intelligencePriority;
    }

    public List<ModelHint> hints() {
        return hints;
    }

    public Double costPriority() {
        return costPriority;
    }

    public Double speedPriority() {
        return speedPriority;
    }

    public Double intelligencePriority() {
        return intelligencePriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelPreferences that = (ModelPreferences) o;
        return Objects.equals(hints, that.hints) &&
               Objects.equals(costPriority, that.costPriority) &&
               Objects.equals(speedPriority, that.speedPriority) &&
               Objects.equals(intelligencePriority, that.intelligencePriority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hints, costPriority, speedPriority, intelligencePriority);
    }

    @Override
    public String toString() {
        return "ModelPreferences{" +
               "hints=" + hints +
               ", costPriority=" + costPriority +
               ", speedPriority=" + speedPriority +
               ", intelligencePriority=" + intelligencePriority +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<ModelHint> hints;
        private Double costPriority;
        private Double speedPriority;
        private Double intelligencePriority;

        public Builder hints(List<ModelHint> hints) {
            this.hints = hints;
            return this;
        }

        public Builder addHint(String name) {
            if (this.hints == null) {
                this.hints = new ArrayList<>();
            }
            this.hints.add(new ModelHint(name));
            return this;
        }

        public Builder costPriority(Double costPriority) {
            this.costPriority = costPriority;
            return this;
        }

        public Builder speedPriority(Double speedPriority) {
            this.speedPriority = speedPriority;
            return this;
        }

        public Builder intelligencePriority(Double intelligencePriority) {
            this.intelligencePriority = intelligencePriority;
            return this;
        }

        public ModelPreferences build() {
            return new ModelPreferences(hints, costPriority, speedPriority, intelligencePriority);
        }
    }
}
