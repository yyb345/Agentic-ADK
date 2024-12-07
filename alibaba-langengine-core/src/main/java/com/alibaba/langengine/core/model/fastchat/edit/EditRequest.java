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
package com.alibaba.langengine.core.model.fastchat.edit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Given a prompt and an instruction, OpenAi will return an edited version of the prompt
 *
 * https://beta.openai.com/docs/api-reference/edits/create
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EditRequest {

    /**
     * The name of the model to use.
     * Required if using the new v1/edits endpoint.
     */
    String model;

    /**
     * The input text to use as a starting point for the edit.
     */
    String input;

    /**
     * The instruction that tells the model how to edit the prompt.
     * For example, "Fix the spelling mistakes"
     */
    @NonNull
    String instruction;

    /**
     * How many edits to generate for the input and instruction.
     */
    Integer n;

    /**
     * What sampling temperature to use. Higher values means the model will take more risks.
     * Try 0.9 for more creative applications, and 0 (argmax sampling) for ones with a well-defined answer.
     *
     * We generally recommend altering this or {@link EditRequest#topP} but not both.
     */
    Double temperature;

    /**
     * An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of
     * the tokens with top_p probability mass.So 0.1 means only the tokens comprising the top 10% probability mass are
     * considered.
     *
     * We generally recommend altering this or {@link EditRequest#temperature} but not both.
     */
    @JsonProperty("top_p")
    Double topP;
}
