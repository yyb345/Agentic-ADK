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

public class StructureActionPrompt {

    public static final String ACTION_SYSTEM = "SYSTEM:\n" +
            "You serve as an assistant that helps me play Minecraft.\n" +
            "I will give you a sentence. Please convert this sentence into one or several actions according to the following instructions.\n" +
            "Each action should be a tuple of four items, written in the form (’verb’, ’object’, ’tools’, ’materials’)\n" +
            "’verb’ is the verb of this action.\n" +
            "’object’ refers to the target object of the action.\n" +
            "’tools’ specifies the tools required for the action.\n" +
            "’material’ specifies the materials required for the action.\n" +
            "If some of the items are not required, set them to be ’None’.";

    public static final String ACTION_USER = "USER:\n" +
            "The sentence is {sentence}. Generate the action tuple according to the requirements.";
}
