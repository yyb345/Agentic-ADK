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
 * @author: aihe.ah
 * @date: 2025/4/2
 * 功能描述：
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class JSONRPCRequest implements JSONRPCMessage {
    private final String jsonrpc;
    private final String method;
    private final Object id;
    private final Object params;

    public JSONRPCRequest(
            @JsonProperty("jsonrpc") String jsonrpc,
            @JsonProperty("method") String method,
            @JsonProperty("id") Object id,
            @JsonProperty("params") Object params) {
        this.jsonrpc = jsonrpc;
        this.method = method;
        this.id = id;
        this.params = params;
    }

    @Override
    public String jsonrpc() {
        return jsonrpc;
    }

    public String method() {
        return method;
    }

    public Object id() {
        return id;
    }

    public Object params() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSONRPCRequest that = (JSONRPCRequest) o;
        return Objects.equals(jsonrpc, that.jsonrpc) &&
                Objects.equals(method, that.method) &&
                Objects.equals(id, that.id) &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonrpc, method, id, params);
    }

    @Override
    public String toString() {
        return "JSONRPCRequest{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", method='" + method + '\'' +
                ", id=" + id +
                ", params=" + params +
                '}';
    }
}
