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
package com.alibaba.agentic.core.runner.pipeline;

import com.alibaba.agentic.core.executor.Result;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 17:10
 */
@Component
public class PipelineUtil {

    private static AbstractPipeline pipeline;

    @Autowired
    private AbstractPipeline abstractPipeline;

    public static Flowable<Result> doPipe(PipelineRequest request) {
        return PipelineUtil.pipeline.doPipes(request);
    }

    @PostConstruct
    public void init() {
        PipelineUtil.pipeline = abstractPipeline;
    }

}
