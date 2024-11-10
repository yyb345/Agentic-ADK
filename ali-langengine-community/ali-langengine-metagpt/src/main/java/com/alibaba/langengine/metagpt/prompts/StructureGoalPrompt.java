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

public class StructureGoalPrompt {

    public static final String GOAL_SYSTEM = "SYSTEM:\n" +
            "You are an assistant for the game Minecraft.\n" +
            "I will give you some target object and some knowledge related to the object. Please write the obtaining of the object as a goal in the standard form.\n" +
            "The standard form of the goal is as follows:\n" +
            "{\n" +
            "\"object\": \"the name of the target object\",\n" +
            "\"count\": \"the target quantity\",\n" +
            "\"material\": \"the materials required for this goal, a dictionary in the form {material_name: material_quantity}. If no material is required, set it to None\",\n" +
            "\"tool\": \"the tool used for this goal. If multiple tools can be used for this goal, only write the most basic one. If no tool is required, set it to None\",\n" +
            "\"info\": \"the knowledge related to this goal\"\n" +
            "}\n" +
            "The information I will give you:\n" +
            "Target object: the name and the quantity of the target object\n" +
            "Knowledge: some knowledge related to the object.\n" +
            "Requirements:\n" +
            "1. You must generate the goal based on the provided knowledge instead of purely depending on your own knowledge.\n" +
            "2. The \"info\" should be as compact as possible, at most 3 sentences. The knowledge I give you may be raw texts from Wiki documents. Please extract and summarize important information instead of directly copying all the texts.\n" +
            "Goal Example:\n" +
            "{\n" +
            "\"object\": \"iron_ore\",\n" +
            "\"count\": 1,\n" +
            "\"material\": None,\n" +
            "\"tool\": \"stone_pickaxe\",\n" +
            "\"info\": \"iron ore is obtained by mining iron ore. iron ore is most found in level 53. iron ore can only be mined with a stone pickaxe or better; using a wooden or gold pickaxe will yield nothing.\"\n" +
            "}\n" +
            "{\n" +
            "\"object\": \"wooden_pickaxe\",\n" +
            "\"count\": 1,\n" +
            "\"material\": {\"planks\": 3, \"stick\": 2},\n" +
            "\"tool\": \"crafting_table\",\n" +
            "\"info\": \"wooden pickaxe can be crafted with 3 planks and 2 stick as the material and crafting table as the tool.\"\n" +
            "}";

    public static final String GOAL_USER = "USER:\n" +
            "Target object: {object quantity} {object name}\n" +
            "Knowledge: {related knowledge}";
}
