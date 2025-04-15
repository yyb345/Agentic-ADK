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
 */ // ---------------------------
// Initialization
// ---------------------------
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class InitializeRequest implements McpSchema.Request {
    private final String protocolVersion;
    private final ClientCapabilities capabilities;
    private final Implementation clientInfo;

    public InitializeRequest(
            @JsonProperty("protocolVersion") String protocolVersion,
            @JsonProperty("capabilities") ClientCapabilities capabilities,
            @JsonProperty("clientInfo") Implementation clientInfo) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
        this.clientInfo = clientInfo;
    }

    public String protocolVersion() {
        return protocolVersion;
    }

    public ClientCapabilities capabilities() {
        return capabilities;
    }

    public Implementation clientInfo() {
        return clientInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitializeRequest that = (InitializeRequest) o;
        return Objects.equals(protocolVersion, that.protocolVersion) &&
                Objects.equals(capabilities, that.capabilities) &&
                Objects.equals(clientInfo, that.clientInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolVersion, capabilities, clientInfo);
    }

    @Override
    public String toString() {
        return "InitializeRequest{" +
                "protocolVersion='" + protocolVersion + '\'' +
                ", capabilities=" + capabilities +
                ", clientInfo=" + clientInfo +
                '}';
    }
}
