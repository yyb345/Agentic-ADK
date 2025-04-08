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
package com.alibaba.langengine.core.model.fastchat.engine;

import lombok.Data;

/**
 * GPT-3 engine details
 *
 * https://beta.openai.com/docs/api-reference/retrieve-engine
 */
@Deprecated
@Data
public class Engine {
    /**
     * An identifier for this engine, used to specify an engine for completions or searching.
     */
    public String id;

    /**
     * The type of object returned, should be "engine"
     */
    public String object;

    /**
     * The owner of the GPT-3 engine, typically "openai"
     */
    public String owner;

    /**
     * Whether the engine is ready to process requests or not
     */
    public boolean ready;
}
