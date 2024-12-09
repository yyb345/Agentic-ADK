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
package com.alibaba.langengine.metagpt.roles;

import com.alibaba.langengine.metagpt.Environment;
import com.alibaba.langengine.metagpt.Message;
import com.alibaba.langengine.metagpt.actions.Action;
import com.alibaba.langengine.metagpt.memory.Memory;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Role Runtime Context
 *
 * @author xiaoxuan.lp
 */
@Data
public class RoleContext {

    private Environment env;

    private Memory memory = new Memory();

//    private LongTermMemory longTermMemory;

    private int state = 0;

    private Action todo;

    private HashSet<Class<? extends Action>> watch = new HashSet<>();

    private List<Message> news = new ArrayList<>();

    /**
     * Get the information corresponding to the watched actions
     * @return
     */
    public List<Message> getImportantMemory() {
        // Get the information corresponding to the watched actions
        return this.memory.getByActions(this.watch);
    }

    public List<Message> getHistory() {
        return this.memory.get(0);
    }
}
