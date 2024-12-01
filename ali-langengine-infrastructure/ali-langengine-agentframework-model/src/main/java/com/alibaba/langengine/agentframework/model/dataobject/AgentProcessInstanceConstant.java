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
package com.alibaba.langengine.agentframework.model.dataobject;

public class AgentProcessInstanceConstant {

    public static final Integer STATUS_RUNNING = 0;
    public static final Integer STATUS_PAUSE = 1;
    public static final Integer STATUS_EXCEPTION = 2;
    public static final Integer STATUS_FINISHED = 3;
    public static final Integer STATUS_LOCKED = 4;

    public static final Integer RETRY_NO = 0;
    public static final Integer RETRY_STEP = 1;
    public static final Integer RETRY_FIXED = 2;
}
