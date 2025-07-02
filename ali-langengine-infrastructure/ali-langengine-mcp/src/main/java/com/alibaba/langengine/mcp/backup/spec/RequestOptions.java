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
//package com.alibaba.langengine.mcp.spec;
//
//import com.alibaba.langengine.mcp.shared.ProgressCallback;
//
//
///**
// * Options that can be given per request.
// */
//public class RequestOptions {
//
//    ProgressCallback onProgress;
//
//    Long timeout = 60 * 1000l;
//
//    public RequestOptions(ProgressCallback onProgress, Long timeout) {
//        setOnProgress(onProgress);
//        setTimeout(timeout);
//    }
//
//    public ProgressCallback getOnProgress() {
//        return onProgress;
//    }
//
//    public void setOnProgress(ProgressCallback onProgress) {
//        this.onProgress = onProgress;
//    }
//
//    public Long getTimeout() {
//        return timeout;
//    }
//
//    public void setTimeout(Long timeout) {
//        this.timeout = timeout;
//    }
//}
