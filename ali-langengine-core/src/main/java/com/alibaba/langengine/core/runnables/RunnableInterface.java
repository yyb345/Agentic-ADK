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
package com.alibaba.langengine.core.runnables;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Runnable Interface.
 * A unit of work that can be invoked, batched, streamed, transformed and composed.
 *
 * @author xiaoxuan.lp
 */
public interface RunnableInterface<Input, Output> {

    /**
     * Runnable name
     *
     * @return
     */
    String getName();

    /**
     * call the chain on an input
     *
     * @param input
     * @return
     */
    Output invoke(Input input);

    /**
     * call the chain on an input
     *
     * @param input
     * @param config
     * @return
     */
    Output invoke(Input input, RunnableConfig config);

    /**
     * call the chain on an input async
     *
     * @param input
     * @return
     */
    CompletableFuture<Output> invokeAsync(Input input);

    /**
     * call the chain on an input async
     *
     * @param input
     * @param config
     * @return
     */
    CompletableFuture<Output> invokeAsync(Input input, RunnableConfig config);

    /**
     * Default implementation of stream, which calls invoke.
     * Subclasses should override this method if they support streaming output.
     *
     * @param input
     * @param chunkConsumer
     * @return
     */
    Output stream(Input input, Consumer<Object> chunkConsumer);

    /**
     * Default implementation of stream, which calls invoke.
     * Subclasses should override this method if they support streaming output.
     *
     * @param input
     * @param config
     * @param chunkConsumer
     * @return
     */
    Output stream(Input input, RunnableConfig config, Consumer<Object> chunkConsumer);

    /**
     * Default implementation of stream, which calls invoke.
     * Subclasses should override this method if they support streaming output with log steps.
     *
     * @param input
     * @param chunkConsumer
     * @return
     */
    Output streamLog(Input input, Consumer<Object> chunkConsumer);

    /**
     * Default implementation of stream, which calls invoke.
     * Subclasses should override this method if they support streaming output.
     *
     * @param input
     * @param config
     * @param chunkConsumer
     * @return
     */
    Output streamLog(Input input, RunnableConfig config, Consumer<Object> chunkConsumer);

    /**
     * Default implementation of stream, which calls invoke.
     * Subclasses should override this method if they support streaming output with log steps.
     *
     * @param input
     * @param chunkConsumer
     * @return
     */
    CompletableFuture<Output> streamAsync(Input input, Consumer<Object> chunkConsumer);

    /**
     * Default implementation of stream, which calls invoke.
     * Subclasses should override this method if they support streaming output.
     *
     * @param input
     * @param config
     * @param chunkConsumer
     * @return
     */
    CompletableFuture<Output> streamAsync(Input input, RunnableConfig config, Consumer<Object> chunkConsumer);

    /**
     * Default implementation of stream, which calls invoke.
     * Subclasses should override this method if they support streaming output with log steps.
     *
     * @param input
     * @param chunkConsumer
     * @return
     */
    CompletableFuture<Output> streamLogAsync(Input input, Consumer<Object> chunkConsumer);

    /**
     * Default implementation of stream, which calls invoke.
     * Subclasses should override this method if they support streaming output with log steps.
     *
     * @param input
     * @param config
     * @param chunkConsumer
     * @return
     */
    CompletableFuture<Output> streamLogAsync(Input input, RunnableConfig config, Consumer<Object> chunkConsumer);

    /**
     * Default implementation runs invoke in parallel
     *
     * @param inputs
     * @return
     */
    List<Output> batch(List<Input> inputs);

    /**
     * Default implementation runs invoke in parallel
     *
     * @param inputs
     * @return
     */
    List<Output> batch(List<Input> inputs, RunnableConfig config);

    /**
     * Default implementation runs invoke async in parallel
     *
     * @param inputs
     * @return
     */
    CompletableFuture<List<Output>> batchAsync(List<Input> inputs);

    /**
     * Default implementation runs invoke async in parallel
     *
     * @param inputs
     * @param config
     * @return
     */
    CompletableFuture<List<Output>> batchAsync(List<Input> inputs, RunnableConfig config);

    /**
     * Sometimes we want to invoke a Runnable within a Runnable sequence with constant arguments
     * that are not part of the output of the preceding Runnable in the sequence, and which are not part of the user input.
     * We can use Runnable.bind() to easily pass these arguments in.
     *
     * @param extraAttributes
     * @return
     */
    RunnableInterface<Input, Output> bind(Map<String, Object> extraAttributes);

    /**
     * Add fallbacks to a runnable, returning a new Runnable.
     *
     * @param fallbacks
     * @return
     */
    RunnableInterface<Input, Output> withFallbacks(RunnableInterface<Input, Output>... fallbacks);

    /**
     * Create a new Runnable that retries the original runnable on exceptions.
     *
     * @param maxAttemptNumber
     * @param extraAttributes
     * @return
     */
    RunnableInterface<Input, Output> withRetry(Integer maxAttemptNumber, Map<String, Object> extraAttributes);

    /**
     * A description of the inputs accepted by a Runnable.
     *
     * @return
     */
    String getInputSchema();

    /**
     * A description of the outputs produced by a Runnable.
     *
     * @return
     */
    String getOutputSchema();
}
