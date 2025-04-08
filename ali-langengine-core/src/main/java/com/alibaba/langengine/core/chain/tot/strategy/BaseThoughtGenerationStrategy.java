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
package com.alibaba.langengine.core.chain.tot.strategy;

import com.alibaba.langengine.core.chain.LLMChain;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Base class for a thought generation strategy.
 * 思想生成策略的基类。
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class BaseThoughtGenerationStrategy extends LLMChain {

    /**
     * The number of children thoughts to propose at each step.
     * 每一步提出的子想法数量。
     */
    private int c = 3;

    /**
     * Generate the next thought given the problem description and the thoughts generated so far.
     * 根据问题描述和到目前为止产生的想法，产生下一个想法。
     *
     * @param problemDescription
     * @param thoughtsPath
     * @param extraAttributes
     * @return
     */
    public abstract String nextThought(String problemDescription, List<String> thoughtsPath, Map<String, Object> extraAttributes);

}
