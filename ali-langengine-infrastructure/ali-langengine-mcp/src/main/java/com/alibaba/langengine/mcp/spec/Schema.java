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

import java.util.Arrays;
import java.util.List;

public class Schema {

    public static final String LATEST_PROTOCOL_VERSION = "2024-11-05";

    public static final List<String> SUPPORTED_PROTOCOL_VERSIONS = Arrays.asList(new String[] {
            LATEST_PROTOCOL_VERSION,
            "2024-10-07",
    });

    public static final String JSONRPC_VERSION = "2.0";

    // ---------------------------
    // JSON-RPC Error Codes
    // ---------------------------
    public final class ErrorCodes {

        public static final int PARSE_ERROR = -32700;

        public static final int INVALID_REQUEST = -32600;

        public static final int METHOD_NOT_FOUND = -32601;

        public static final int INVALID_PARAMS = -32602;

        public static final int INTERNAL_ERROR = -32603;
    }
}
