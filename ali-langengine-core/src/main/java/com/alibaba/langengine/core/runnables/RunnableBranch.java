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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A Runnable that selects which branch to run based on a condition.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class RunnableBranch extends Runnable<Object, RunnableOutput> {

    private List<Pair<RunnableLambda, RunnableInterface>> branches;

    private RunnableInterface defaultBranch;

    public RunnableBranch(Pair<RunnableLambda, RunnableInterface>... branches) {
        init(null, branches);
    }

    public RunnableBranch(String name, Pair<RunnableLambda, RunnableInterface>... branches) {
        setName(name);
        init(null, branches);
    }

    public RunnableBranch(RunnableInterface defaultBranch, Pair<RunnableLambda, RunnableInterface>... branches) {
        init(defaultBranch, branches);
    }

    public RunnableBranch(String name, RunnableInterface defaultBranch, Pair<RunnableLambda, RunnableInterface>... branches) {
        setName(name);
        init(defaultBranch, branches);
    }

    private void init(RunnableInterface defaultBranch, Pair<RunnableLambda, RunnableInterface>... branches) {
        if(branches != null && branches.length < 2) {
            throw new RuntimeException("RunnableBranch requires at least two branches");
        }
        if(defaultBranch != null) {
            setDefaultBranch(defaultBranch);
            setBranches(Arrays.stream(branches).collect(Collectors.toList()));
        } else {
            setDefaultBranch(branches[branches.length - 1].getValue());
            setBranches(Arrays.stream(branches).limit(branches.length - 1).collect(Collectors.toList()));
        }
    }


    @Override
    public RunnableOutput invoke(Object input, RunnableConfig config) {
        Object output = null;
        try {
            boolean find = false;
            for (Pair<RunnableLambda, RunnableInterface> branch : getBranches()) {
                RunnableLambda condition = branch.getKey();
                RunnableInterface runnable = branch.getValue();
                Object expressionValue = condition.invoke((RunnableHashMap) input, config);
                if (expressionValue instanceof Boolean && ((Boolean)expressionValue)) {
                    output = runnable.invoke(input, config);
                    find = true;
                    break;
                }
            }
            if (!find) {
                output = defaultBranch.invoke(input, config);
            }

            if (output == null) {
                return null;
            }
            if (output instanceof RunnableOutput) {
                return (RunnableOutput)output;
            }
            return null;
        } catch (Throwable e) {
            log.error("RunnableBranch invoke error", e);
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public RunnableOutput stream(Object input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        Object output = null;
        try {
            boolean find = false;
            for (Pair<RunnableLambda, RunnableInterface> branch : getBranches()) {
                RunnableLambda condition = branch.getKey();
                RunnableInterface runnable = branch.getValue();
                Object expressionValue = condition.invoke((RunnableHashMap) input, config);
                if (expressionValue instanceof Boolean && ((Boolean)expressionValue)) {
                    output = runnable.stream(input, config, chunkConsumer);
                    find = true;
                    break;
                }
            }
            if (!find) {
                output = defaultBranch.stream(input, config, chunkConsumer);
            }

            if (output == null) {
                return null;
            }
            if (output instanceof RunnableOutput) {
                return (RunnableOutput)output;
            }
            return null;
        } catch (Throwable e) {
            log.error("RunnableBranch invoke error", e);
            throw new RuntimeException(e.toString());
        }
    }
}
