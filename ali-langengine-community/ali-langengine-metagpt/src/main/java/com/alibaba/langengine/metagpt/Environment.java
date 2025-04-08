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

import com.alibaba.langengine.metagpt.memory.Memory;
import com.alibaba.langengine.metagpt.roles.Role;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 环境，承载一批角色，角色可以向环境发布消息，可以被其他角色观察到
 * Environment, hosting a batch of roles, roles can publish messages to the environment, and can be observed by other roles
 *
 * @author xiaoxuan.lp
 */
@Data
public class Environment {
    //工作空间
    private String workspace = "metagpt_workspace/";
    //项目编号，用于项目的启动、暂停、恢复
    private String projectCode = "DEFAULT";


    private Map<String, Role> roles = new LinkedHashMap<>();
    private Memory memory = new Memory();
    private String history = "";

    public void addRole(Role role) {
        role.setEnv(this);
        roles.put(role.getProfile(), role);
    }

    public void addRoles(Iterable<Role> roles) {
        for (Role role : roles) {
            addRole(role);
        }
    }

    public void publishMessage(Message message) {
        memory.add(message);
        history += "\n" + message.toString();
    }

    public List<Message> run(int k){
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            for (Map.Entry<String, Role> entry : roles.entrySet()) {
                Role role = entry.getValue();
                Message message = role.run();
                messages.add(message);
            }
        }
        return messages;
    }

    public Role getRole(String name) {
        return roles.getOrDefault(name, null);
    }
}
