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
package com.alibaba.langengine.mcp.spec.schema;

import com.alibaba.langengine.mcp.spec.Implementation;
import com.alibaba.langengine.mcp.spec.ServerCapabilities;

public class InitializeResult {

    private String protocolVersion;

    private ServerCapabilities capabilities;

    private Implementation serverInfo;

    private String instructions;

    public InitializeResult(String protocolVersion, ServerCapabilities capabilities, Implementation serverInfo, String instructions) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
        this.serverInfo = serverInfo;
        this.instructions = instructions;
    }

    public Implementation getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(Implementation serverInfo) {
        this.serverInfo = serverInfo;
    }

    public ServerCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(ServerCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
