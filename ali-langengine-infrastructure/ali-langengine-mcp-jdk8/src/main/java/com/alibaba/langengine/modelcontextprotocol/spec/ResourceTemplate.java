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

import java.util.Objects;

/**
 * Resource templates allow servers to expose parameterized resources using URI
 * templates.
 * 
 * JDK 1.8 compatible version.
 * 
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6570">RFC 6570</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ResourceTemplate implements Annotated {
    private final String uriTemplate;
    private final String name;
    private final String description;
    private final String mimeType;
    private final Annotations annotations;

    public ResourceTemplate(
            @JsonProperty("uriTemplate") String uriTemplate,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("mimeType") String mimeType,
            @JsonProperty("annotations") Annotations annotations) {
        this.uriTemplate = uriTemplate;
        this.name = name;
        this.description = description;
        this.mimeType = mimeType;
        this.annotations = annotations;
    }

    public String uriTemplate() {
        return uriTemplate;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String mimeType() {
        return mimeType;
    }

    @Override
    public Annotations annotations() {
        return annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceTemplate that = (ResourceTemplate) o;
        return Objects.equals(uriTemplate, that.uriTemplate) &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(mimeType, that.mimeType) &&
               Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uriTemplate, name, description, mimeType, annotations);
    }

    @Override
    public String toString() {
        return "ResourceTemplate{" +
               "uriTemplate='" + uriTemplate + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", mimeType='" + mimeType + '\'' +
               ", annotations=" + annotations +
               '}';
    }
}
