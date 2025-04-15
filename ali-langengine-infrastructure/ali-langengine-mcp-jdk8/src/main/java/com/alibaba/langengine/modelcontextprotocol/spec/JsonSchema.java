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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JSON Schema representation.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class JsonSchema {
    private final String type;
    private final Map<String, Object> properties;
    private final List<String> required;
    private final Boolean additionalProperties;

    public JsonSchema(
            @JsonProperty("type") String type,
            @JsonProperty("properties") Map<String, Object> properties,
            @JsonProperty("required") List<String> required,
            @JsonProperty("additionalProperties") Boolean additionalProperties) {
        this.type = type;
        this.properties = properties;
        this.required = required;
        this.additionalProperties = additionalProperties;
    }

    public String type() {
        return type;
    }

    public Map<String, Object> properties() {
        return properties;
    }

    public List<String> required() {
        return required;
    }

    public Boolean additionalProperties() {
        return additionalProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonSchema that = (JsonSchema) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(properties, that.properties) &&
               Objects.equals(required, that.required) &&
               Objects.equals(additionalProperties, that.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, properties, required, additionalProperties);
    }

    @Override
    public String toString() {
        return "JsonSchema{" +
               "type='" + type + '\'' +
               ", properties=" + properties +
               ", required=" + required +
               ", additionalProperties=" + additionalProperties +
               '}';
    }
}
