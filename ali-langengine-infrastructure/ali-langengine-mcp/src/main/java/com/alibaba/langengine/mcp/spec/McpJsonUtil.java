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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.mcp.spec.schema.JsonSchema;

import java.io.IOException;

public class McpJsonUtil {

    public static JSONRPCMessage deserializeJsonRpcMessage(String jsonText) throws IOException {
        JSONObject map = JSON.parseObject(jsonText);

        // Determine message type based on specific JSON structure
        if (map.containsKey("method") && map.containsKey("id")) {
            return map.toJavaObject(JSONRPCRequest.class);
        } else if (map.containsKey("method") && !map.containsKey("id")) {
            return map.toJavaObject(JSONRPCNotification.class);
        } else if (map.containsKey("result") || map.containsKey("error")) {
            return map.toJavaObject(JSONRPCResponse.class);
        }

        throw new IllegalArgumentException("Cannot deserialize JSONRPCMessage: " + jsonText);
    }

    public static JsonSchema parseSchema(String schema) {
        try {
            return JSON.parseObject(schema, JsonSchema.class);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid schema: " + schema, e);
        }
    }
}
