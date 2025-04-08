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
//package com.alibaba.langengine.mcp.spec.backup;
//
//import com.alibaba.langengine.mcp.spec.ClientNotification;
//import com.alibaba.langengine.mcp.spec.Method;
//import com.alibaba.langengine.mcp.spec.MethodDefined;
//
///**
// * This notification is sent from the client to the server after initialization has finished.
// */
//public class InitializedNotification implements ClientNotification {
//
//    Method method = MethodDefined.NotificationsInitialized;
//
//    @Override
//    public Method getMethod() {
//        return method;
//    }
//
//    public void setMethod(Method method) {
//        this.method = method;
//    }
//}
