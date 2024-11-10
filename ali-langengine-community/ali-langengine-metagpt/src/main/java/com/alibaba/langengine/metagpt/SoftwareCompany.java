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
package com.alibaba.langengine.metagpt;

import com.alibaba.langengine.metagpt.actions.BossRequirement;
import com.alibaba.langengine.metagpt.roles.Role;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 软件公司：拥有团队、SOP（标准操作流程）、即时通讯平台，专门编写可执行代码。
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class SoftwareCompany {

    private Environment environment = new Environment();

    private float investment = 10.0f;

    private String idea = "";

    public void hire(List<Role> roles) {
        environment.addRoles(roles);
    }

    public void startProject(String idea, String workspace, String projectCode) {
        startProject(idea);
        this.environment.setWorkspace(workspace);
        this.environment.setProjectCode(projectCode);
    }
    public void startProject(String idea) {
        this.idea = idea;
        Message message = new Message();
        message.setRole("BOSS");
        message.setContent(idea);
        message.setCauseBy(BossRequirement.class);
        environment.publishMessage(message);
    }

    public List<Message> run(int nRound) {
        return runCompany(nRound);
    }

    private List<Message> runCompany(int nRound) {
        if (nRound > 0) {
            log.info("nRound=" + nRound);
            List<Message> messages = environment.run(1);
            return messages;
        } else {
            // TODO ...
            return null;
        }
    }
}
