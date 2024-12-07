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
package com.alibaba.langengine.sqlite.memory.cache;

import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SqliteConfig.class})
public class SqliteCacheTest {

    @Resource
    private SqliteCache sqliteCache;

    @Test
    public void test_OpenAI_predict() {
        // success
        LangEngineConfiguration.CurrentCache = sqliteCache;

        ChatOpenAI llm = new ChatOpenAI();
        llm.setN(1);
        llm.setBestOf(1);

        long start = System.currentTimeMillis();
        String result = llm.predict("hello");
        System.out.println(result);
        System.out.println((System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        result = llm.predict("hello");
        System.out.println(result);
        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}
