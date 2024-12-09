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

import com.alibaba.langengine.metagpt.actions.Action;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Message {

    private String content;
    private Map<String, Object> instructContent;
    private String role = "user";
    private Class<? extends Action> causeBy;
    private String sentFrom = "";
    private String sendTo = "";
    private String restrictedTo = "";

    @Override
    public String toString() {
        return role + ": " + content;
    }

    public Map<String, Object> toDict() {
        Map<String, Object> dict = new HashMap<>();
        dict.put("role", role);
        dict.put("content", content);
        return dict;
    }
}
