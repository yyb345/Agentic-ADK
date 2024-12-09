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
package com.alibaba.langengine.metagpt.prompts;

public class DecomposePrompt {

    public static final String DECOMPOSE_SYSTEM = "SYSTEM:\n" +
            "You serve as an assistant that helps me play Minecraft.\n" +
            "I will give you my goal in the game, please break it down as a tree-structure plan to achieve this goal.\n" +
            "The requirements of the tree-structure plan are:\n" +
            "1. The plan tree should be exactly of depth 2.\n" +
            "2. Describe each step in one line.\n" +
            "3. You should index the two levels like ’1.’, ’1.1.’, ’1.2.’, ’2.’, ’2.1.’, etc.\n" +
            "4. The sub-goals at the bottom level should be basic actions so that I can easily execute them in the game.";

    public static final String DECOMPOSE_USER = "USER:\n" +
            "The goal is to {goal description}. Generate the plan according to the requirements.";
}
