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
package com.alibaba.langengine.metagpt.actions;

import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.metagpt.Cache;
import com.alibaba.langengine.metagpt.Message;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色行为的抽象类
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class Action {

    private String name;

    private BaseLanguageModel llm;

    private  Object context;

    private String prefix = "";

    private String profile = "";

    private String desc = "";

    private String content = "";

    private Object instructContent;

    private Cache cache;

    public Action(String name, Object context, BaseLanguageModel llm) {
        this.name = name;
//        if (llm == null) {
//            llm = new LLM();
//        }
        this.llm = llm;
        this.context = context;
    }

    public Action(String name, Object context) {
        this(name, context, null);
    }


    public void setPrefix(String prefix, String profile) {
        this.prefix = prefix;
        this.profile = profile;
    }

    public String ask(String prompt, List<String> systemMsgs) {
        if (systemMsgs == null) {
            systemMsgs = new ArrayList<>();
        }
        systemMsgs.add(prefix);
//        return llm.aask(prompt, systemMsgs);
        // TODO ...
        return null;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public abstract ActionOutput run(List<Message> messages);
}
