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
package com.alibaba.langengine.agentframework.model.enums;

import lombok.Getter;

/**
 * description
 *
 * @Author zhishan
 * @Date 2024-08-01
 */
@Getter
public enum ChatMsgMemTypeEnum {
    /**
     * 正常记忆
     */
    KEEP_IN_MEMORY(0),

    /**
     * 仅用于给用户展示，不作为后续对话的上下文
     */
    NO_NEED_ADD_TO_HISTORY(1),
    ;

    private final int code;

    ChatMsgMemTypeEnum(int code) {
        this.code = code;
    }
}
