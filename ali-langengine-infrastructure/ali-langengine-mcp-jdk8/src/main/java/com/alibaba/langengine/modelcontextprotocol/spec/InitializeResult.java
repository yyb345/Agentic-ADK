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
public class InitializeResult {
    private final String protocolVersion;
    private final ServerCapabilities capabilities;
    private final Implementation serverInfo;
    private final String instructions;

    public InitializeResult(
            @JsonProperty("protocolVersion") String protocolVersion,
            @JsonProperty("capabilities") ServerCapabilities capabilities,
            @JsonProperty("serverInfo") Implementation serverInfo,
            @JsonProperty("instructions") String instructions) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
        this.serverInfo = serverInfo;
        this.instructions = instructions;
    }

    public String protocolVersion() {
        return protocolVersion;
    }

    public ServerCapabilities capabilities() {
        return capabilities;
    }

    public Implementation serverInfo() {
        return serverInfo;
    }

    public String instructions() {
        return instructions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitializeResult that = (InitializeResult) o;
        return Objects.equals(protocolVersion, that.protocolVersion) &&
                Objects.equals(capabilities, that.capabilities) &&
                Objects.equals(serverInfo, that.serverInfo) &&
                Objects.equals(instructions, that.instructions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolVersion, capabilities, serverInfo, instructions);
    }

    @Override
    public String toString() {
        return "InitializeResult{" +
                "protocolVersion='" + protocolVersion + '\'' +
                ", capabilities=" + capabilities +
                ", serverInfo=" + serverInfo +
                ", instructions='" + instructions + '\'' +
                '}';
    }
}
