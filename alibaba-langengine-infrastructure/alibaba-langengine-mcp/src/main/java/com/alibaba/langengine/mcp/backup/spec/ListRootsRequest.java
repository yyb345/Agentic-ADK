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
//package com.alibaba.langengine.mcp.spec.schema;
//
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.langengine.mcp.spec.Method;
//import com.alibaba.langengine.mcp.spec.MethodDefined;
//import com.alibaba.langengine.mcp.spec.ServerRequest;
//import com.alibaba.langengine.mcp.spec.WithMeta;
//
///**
// * Sent from the server to request a list of root URIs from the client.
// */
//public class ListRootsRequest implements ServerRequest, WithMeta {
//
//    private Method method = MethodDefined.RootsList;
//    private JSONObject meta;
//
//    public ListRootsRequest(JSONObject meta) {
//        this.meta = meta;
//    }
//
//    @Override
//    public Method getMethod() {
//        return method;
//    }
//
//    public void setMethod(Method method) {
//        this.method = method;
//    }
//
//    @Override
//    public JSONObject getMeta() {
//        return meta;
//    }
//
//    public void setMeta(JSONObject meta) {
//        this.meta = meta;
//    }
//}
