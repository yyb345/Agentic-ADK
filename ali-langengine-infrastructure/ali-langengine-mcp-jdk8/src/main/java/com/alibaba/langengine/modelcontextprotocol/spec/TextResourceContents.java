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
 * Text contents of a resource.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TextResourceContents implements ResourceContents {
    private final String uri;
    private final String mimeType;
    private final String text;

    public TextResourceContents(
            @JsonProperty("uri") String uri,
            @JsonProperty("mimeType") String mimeType,
            @JsonProperty("text") String text) {
        this.uri = uri;
        this.mimeType = mimeType;
        this.text = text;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public String mimeType() {
        return mimeType;
    }

    public String text() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextResourceContents that = (TextResourceContents) o;
        return Objects.equals(uri, that.uri) &&
               Objects.equals(mimeType, that.mimeType) &&
               Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, mimeType, text);
    }

    @Override
    public String toString() {
        return "TextResourceContents{" +
               "uri='" + uri + '\'' +
               ", mimeType='" + mimeType + '\'' +
               ", text='" + text + '\'' +
               '}';
    }
}
