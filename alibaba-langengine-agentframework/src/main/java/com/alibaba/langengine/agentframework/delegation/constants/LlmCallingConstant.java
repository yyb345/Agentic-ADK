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
package com.alibaba.langengine.agentframework.delegation.constants;

public interface LlmCallingConstant {

    /**
     * prompt
     */
    String PROMPT_KEY = "prompt";

    String MODEL_ID_KEY = "modelId";

    String MODEL_NAME_KEY = "modelName";

    String TEMPERATURE_KEY = "temperature";

    String LLM_TYPE_KEY = "llmType";

    String LLM_TEMPLATE_CODE_KEY = "llmTemplateCode";

    String LLM_CLASS_KEY = "llmClass";

    String HAS_HISTORY_KEY = "hasHistory";

    String OUTPUT_PARSER_KEY = "outputParser";

    String VLINPUTPARAMETERS_PROPERTY_KEY = "vlInputParameters";

    /**
     * 当前中台专用
     */
    String STREAM_REFERENCE = "streamReference";

    String RESPONSE_FILTER_KEY = "responseFilter";

    String CURRENT_NODE_ID = "currentNodeId";

    String STREAM_KEY = "stream";

    String HAS_HISTORY_TRUE = "1";

    String CODE_ENABLE_EXCEPTION_CONFIG = "enableExceptionConfig";
    String CODE_EXCEPTION_CONFIG = "exceptionConfig";
}
