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
import com.alibaba.langengine.metagpt.actions.WriteTasks;
import com.alibaba.langengine.metagpt.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Project Manager role responsible for overseeing project execution and team efficiency.
 *
 *     Attributes:
 *         name (str): Name of the project manager.
 *         profile (str): Role profile, default is 'Project Manager'.
 *         goal (str): Goal of the project manager.
 *         constraints (str): Constraints or limitations for the project manager.
 *
 * @author xiaoxuan.lp
 */
public class ProjectManager extends Role {

    public ProjectManager() {
       this(null);
    }
    public ProjectManager(BaseLanguageModel llm) {
        super("Eve",
                "Project Manager",
                "Improve team efficiency and deliver with quality and quantity",
                "",
                "",
                llm);

        List<Class<? extends Action>> actions = new ArrayList<>();
        actions.add(WriteTasks.class);
        initActions(actions);

        List<Class<? extends Action>> watchActions = new ArrayList<>();
        watchActions.add(WriteDesign.class);
        watch(watchActions);
    }
    @Override
    protected void afterAct(Message msg) {
        String jsonContent = msg.getContent();
        FileUtils.writeFile(getRc().getEnv().getWorkspace() + "task/", "task.json", jsonContent);
    }
}
