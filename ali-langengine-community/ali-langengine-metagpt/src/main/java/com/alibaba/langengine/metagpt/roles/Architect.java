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

import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.metagpt.Message;
import com.alibaba.langengine.metagpt.actions.Action;
import com.alibaba.langengine.metagpt.actions.WriteDesign;
import com.alibaba.langengine.metagpt.actions.WritePRD;
import com.alibaba.langengine.metagpt.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Architect role in a software development process.
 *
 *     Attributes:
 *         name (str): Name of the architect.
 *         profile (str): Role profile, default is 'Architect'.
 *         goal (str): Primary goal or responsibility of the architect.
 *         constraints (str): Constraints or guidelines for the architect.
 *
 * @author xiaoxuan.lp
 */
public class Architect extends Role {

    public Architect() {
        this(null);
    }
    public Architect(BaseLanguageModel llm) {
        super("Bob",
                "Architect",
                "Design a concise, usable, complete python system",
                "Try to specify good open source tools as much as possible",
                "",
                llm);

        List<Class<? extends Action>> actions = new ArrayList<>();
        actions.add(WriteDesign.class);
        initActions(actions);

        List<Class<? extends Action>> watchActions = new ArrayList<>();
        watchActions.add(WritePRD.class);
        watch(watchActions);
    }
    @Override
    protected void afterAct(Message msg) {
        String jsonContent = msg.getContent();
        FileUtils.writeFile(getRc().getEnv().getWorkspace() + "architect/", "architect.json", jsonContent);
    }
}
