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
//package com.alibaba.langengine.mcp.server.backup;
//
//import com.alibaba.langengine.mcp.spec.schema.resources.ReadResourceRequest;
//import com.alibaba.langengine.mcp.spec.schema.resources.ReadResourceResult;
//import com.alibaba.langengine.mcp.spec.schema.resources.Resource;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.function.Function;
//
//public class RegisteredResource {
//
//    Resource resource;
//
//    Function<ReadResourceRequest, CompletableFuture<ReadResourceResult>> readHandler;
//
//    public RegisteredResource(Resource resource, Function<ReadResourceRequest, CompletableFuture<ReadResourceResult>> readHandler) {
//        this.resource = resource;
//        this.readHandler = readHandler;
//    }
//
//    public Resource getResource() {
//        return resource;
//    }
//
//    public void setResource(Resource resource) {
//        this.resource = resource;
//    }
//
//    public Function<ReadResourceRequest, CompletableFuture<ReadResourceResult>> getReadHandler() {
//        return readHandler;
//    }
//
//    public void setReadHandler(Function<ReadResourceRequest, CompletableFuture<ReadResourceResult>> readHandler) {
//        this.readHandler = readHandler;
//    }
//}
