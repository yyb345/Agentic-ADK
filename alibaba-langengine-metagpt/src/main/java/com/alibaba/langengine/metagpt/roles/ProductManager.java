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
import com.alibaba.langengine.metagpt.actions.BossRequirement;
import com.alibaba.langengine.metagpt.actions.WritePRD;
import com.alibaba.langengine.metagpt.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductManager extends Role {

    public ProductManager() {
        this(null);
    }

    public ProductManager(BaseLanguageModel llm) {
        super("Alice",
                "Product Manager",
                "Efficiently create a successful product",
                "",
                "",
                llm);
        List<Class<? extends Action>> actions = new ArrayList<>();
        actions.add(WritePRD.class);
        initActions(actions);

        List<Class<? extends Action>> watchActions = new ArrayList<>();
        watchActions.add(BossRequirement.class);
        watch(watchActions);
    }
    @Override
    protected void afterAct(Message msg) {
        String jsonContent = msg.getContent();
        FileUtils.writeFile(getRc().getEnv().getWorkspace() + "prd/", "prd.json", jsonContent);
    }
}
