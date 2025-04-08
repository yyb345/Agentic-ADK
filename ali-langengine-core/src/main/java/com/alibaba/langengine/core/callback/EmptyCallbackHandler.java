/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.core.callback;

/**
 * @author aihe.ah
 * @time 2023/12/7
 * 功能说明：
 * 在做CSI安全校验的时候，我们只需要在onLlmEnd中获取到llmResult，然后进行校验即可。
 * 并不需要实现这么多的方法，也许有一些其他的场景只需要实现某个方法就可以；
 * 因此创建这个空的视线Handler，如果只需要实现某个方法，继承这个然后进行重写即可。
 */
public class EmptyCallbackHandler extends BaseCallbackHandler {
    @Override
    public void onChainStart(ExecutionContext executionContext) {

    }

    @Override
    public void onChainEnd(ExecutionContext executionContext) {

    }

    @Override
    public void onChainError(ExecutionContext executionContext) {

    }

    @Override
    public void onLlmStart(ExecutionContext executionContext) {

    }

    @Override
    public void onLlmEnd(ExecutionContext executionContext) {

    }

    @Override
    public void onLlmError(ExecutionContext executionContext) {

    }

    @Override
    public void onToolStart(ExecutionContext executionContext) {

    }

    @Override
    public void onToolEnd(ExecutionContext executionContext) {

    }

    @Override
    public void onToolError(ExecutionContext executionContext) {

    }

    @Override
    public void onAgentAction(ExecutionContext executionContext) {

    }

    @Override
    public void onAgentFinish(ExecutionContext executionContext) {

    }

    @Override
    public void onRetrieverStart(ExecutionContext executionContext) {

    }

    @Override
    public void onRetrieverEnd(ExecutionContext executionContext) {

    }

    @Override
    public void onRetrieverError(ExecutionContext executionContext) {

    }
}
