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
package com.alibaba.langengine.demo.callback.support;

import com.alibaba.langengine.openai.model.ChatOpenAI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * MockLLM
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class MockLLM extends ChatOpenAI {

    /**
     * mock开关，true为启动
     */
    private boolean mockSwitch = false;

    /**
     * 休眠时间（毫秒）
     */
    private long internal = 1000l;

    /**
     * 计数器
     */
    private int counter = 0;

    @Override
    public String run(String prompt, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        if(mockSwitch && counter % 2 == 0) {
            setCounter(++counter);
            try {
                Thread.sleep(internal);
                throw new RuntimeException("mock timeout.");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return super.run(prompt, stops, consumer, extraAttributes);
    }
}
