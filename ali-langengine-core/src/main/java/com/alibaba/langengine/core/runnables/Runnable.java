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

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A unit of work that can be invoked, batched, streamed, transformed and composed.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public abstract class Runnable<Input, Output> implements RunnableInterface<Input, Output>, Serializable {

    /**
     * The name of the runnable. Used for debugging and tracing.
     */
    private String name;

    public Output invoke(Input input) {
        return invoke(input, null);
    }


    public abstract Output invoke(Input input, RunnableConfig config);

    public CompletableFuture<Output> invokeAsync(Input input) {
        return invokeAsync(input, null);
    }

    public  CompletableFuture<Output> invokeAsync(Input input, RunnableConfig config) {
        return CompletableFuture.supplyAsync(() -> invoke(input, config));
    }

    public List<Output> batch(List<Input> inputs) {
        return batch(inputs, null);
    }

    public List<Output> batch(List<Input> inputs, RunnableConfig config) {
        try {
            List<CompletableFuture<Output>> futures = Lists.newArrayList();
            for (Input input : inputs) {
                CompletableFuture<Output> future = invokeAsync(input, config);
                futures.add(future);
            }

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            if(config != null) {
                allFutures.get(config.getParallelSecondTimeout(), TimeUnit.SECONDS);
            } else {
                allFutures.get(300, TimeUnit.SECONDS);
            }

            List<Output> outputs = new ArrayList<>();
            for (CompletableFuture<Output> future : futures) {
                outputs.add(future.get());
            }
            return outputs;
        } catch (Throwable e) {
            log.error("Runnable.batch error", e);
            throw new RuntimeException(e.toString());
        }
    }

    public CompletableFuture<List<Output>> batchAsync(List<Input> inputs) {
        return batchAsync(inputs, null);
    }

    public CompletableFuture<List<Output>> batchAsync(List<Input> inputs, RunnableConfig config) {
        return CompletableFuture.supplyAsync(() -> batch(inputs, config));
    }

    public Output stream(Input input, Consumer<Object> chunkConsumer) {
        return stream(input, null, chunkConsumer);
    }

    public abstract Output stream(Input input, RunnableConfig config, Consumer<Object> chunkConsumer);

    public Output streamLog(Input input, Consumer<Object> chunkConsumer) {
        return streamLog(input, null, chunkConsumer);
    }

    public Output streamLog(Input input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return stream(input, config, chunkConsumer);
    }

    public CompletableFuture<Output> streamAsync(Input input, Consumer<Object> chunkConsumer) {
        return streamAsync(input, null, chunkConsumer);
    }

    public CompletableFuture<Output> streamAsync(Input input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return CompletableFuture.supplyAsync(() -> stream(input, config, chunkConsumer));
    }

    public CompletableFuture<Output> streamLogAsync(Input input, Consumer<Object> chunkConsumer) {
        return streamLogAsync(input, null, chunkConsumer);
    }

    public CompletableFuture<Output> streamLogAsync(Input input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return CompletableFuture.supplyAsync(() -> streamLog(input, config, chunkConsumer));
    }

    public RunnableInterface<Input, Output> bind(Map<String, Object> extraAttributes) {
        RunnableBinding runnableBinding = new RunnableBinding();
        runnableBinding.setBound(this);
        runnableBinding.setExtraAttributes(extraAttributes);
        return runnableBinding;
    }

    public RunnableInterface<Input, Output> withFallbacks(RunnableInterface<Input, Output>... fallbacks) {
        RunnableWithFallbacks runnableWithFallbacks = new RunnableWithFallbacks();
        runnableWithFallbacks.setRunnable(this);
        runnableWithFallbacks.setFallbacks(Arrays.asList(fallbacks));
        return runnableWithFallbacks;
    }

    @Override
    public RunnableInterface<Input, Output> withRetry(Integer maxAttemptNumber, Map<String, Object> extraAttributes) {
        RunnableRetry runnableRetry = new RunnableRetry();
        runnableRetry.setMaxAttemptNumber(maxAttemptNumber);
        runnableRetry.setBound(this);
        runnableRetry.setExtraAttributes(extraAttributes);
        return runnableRetry;
    }

    /**
     * 顺序链
     *
     * @param runnables
     * @return
     */
    public static RunnableSequence sequence(RunnableInterface... runnables) {
        return sequence(null, runnables);
    }

    /**
     * 顺序链
     *
     * @param name
     * @param runnables
     * @return
     */
    public static RunnableSequence sequence(String name, RunnableInterface... runnables) {
        return new RunnableSequence(name, runnables);
    }

    /**
     * 并行链
     *
     * @param runnables
     * @return
     */
    public static RunnableParallel parallel(RunnableInterface... runnables) {
        return parallel(null, runnables);
    }

    /**
     * 并行链
     *
     * @param name
     * @param runnables
     * @return
     */
    public static RunnableParallel parallel(String name, RunnableInterface... runnables) {
        return new RunnableParallel(name, runnables);
    }

    /**
     * 遍历
     *
     * @param bound
     * @return
     */
    public static RunnableEach each(RunnableInterface bound) {
        return each(null, bound);
    }

    /**
     * 遍历
     *
     * @param name
     * @param bound
     * @return
     */
    public static RunnableEach each(String name, RunnableInterface bound) {
        RunnableEach runnableEach = new RunnableEach();
        runnableEach.setName(name);
        runnableEach.setBound(bound);
        return runnableEach;
    }

    public static RunnableInputFormatter inputFormatter(String name, Map<String, Function<Object, String>> inputFormatter) {
        return new RunnableInputFormatter(name, inputFormatter);
    }
    public static RunnableInputFormatter inputFormatter(Map<String, Function<Object, String>> inputFormatter) {
        return inputFormatter(null, inputFormatter);
    }
    public static RunnableInputFormatter inputFormatter() {
        return inputFormatter(null, new HashMap<>());
    }
    public static RunnableAssign assign(Map<String, Object> assign) {
        return assign(null, assign);
    }

    public static RunnableAssign assign(String name, Map<String, Object> assign) {
        return new RunnableAssign(name, assign);
    }

    public static RunnablePassthrough passthrough() {
        return new RunnablePassthrough();
    }

    public static RunnableBranch branch(Pair<RunnableLambda, RunnableInterface>... branches) {
        return new RunnableBranch(null, null, branches);
    }

    public static RunnableBranch branch(String name, Pair<RunnableLambda, RunnableInterface>... branches) {
        return new RunnableBranch(name, branches);
    }

    public static RunnableBranch branch(RunnableInterface defaultBranch, Pair<RunnableLambda, RunnableInterface>... branches) {
        return  new RunnableBranch(null, defaultBranch, branches);
    }

    @Override
    public String getInputSchema() {
        return null;
//        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getOutputSchema() {
        return null;
//        throw new UnsupportedOperationException("Not implemented yet");
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
//            JsonSchema schema = schemaGen.generateSchema(getClass());
//            String jsonSchema = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
//            return jsonSchema;
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
    }
}
