/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.runner.pipe;

import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.runner.pipeline.PipelineRequest;
import io.reactivex.rxjava3.core.Flowable;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 17:06
 */
public interface PipeInterface {

    /**
     * pipe执行
     *
     * @param pipelineRequest
     * @return
     */
    Flowable<Result> doPipe(PipelineRequest pipelineRequest);

    /**
     * 是否忽略
     *
     * @param pipelineRequest
     * @return
     */
    boolean ignore(PipelineRequest pipelineRequest);


}
