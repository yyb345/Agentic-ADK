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

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

public class JSONRPCNotification implements JSONRPCMessage {
    private static final String JSONRPC_VERSION = "2.0";

    private String method;

    private Object params;

    private String jsonrpc;

    public JSONRPCNotification(String jsonrpc, String method, Object params) {
        this.method = method;
        this.params = params != null ? params : new HashMap<String, Object>();
        this.jsonrpc = jsonrpc != null ? jsonrpc : JSONRPC_VERSION;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    @Override
    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }
}
