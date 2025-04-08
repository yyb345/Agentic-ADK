///**
// * Copyright (C) 2024 AIDC-AI
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.langengine.mcp.shared;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class McpErrorException extends RuntimeException {
//
//    private final int code;
//    private final String message;
//    private final Map<String, Object> data;
//
//    public McpErrorException(int code, String message,  Map<String, Object> data) {
//        super();
//        this.code = code;
//        this.message = "MCP error " + code + ": " + message;
//        this.data = data != null ? data : new HashMap<>();
//    }
//
//    public McpErrorException(int code, String message) {
//        this(code, message,  new HashMap<>());
//    }
//
//    @Override
//    public String getMessage() {
//        return message;
//    }
//
//    public int getCode() {
//        return code;
//    }
//
//    public Map<String, Object> getData() {
//        return data;
//    }
//}
