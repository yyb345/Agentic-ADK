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
package com.alibaba.langengine.core.model.fastchat.finetune;

import com.alibaba.langengine.core.model.fastchat.file.File;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * An object describing a fine-tuned model. Returned by multiple fine-tune requests.
 *
 * https://beta.openai.com/docs/api-reference/fine-tunes
 */
@Data
public class FineTuneResult {
    /**
     * The ID of the fine-tuning job.
     */
    String id;

    /**
     * The type of object returned, should be "fine-tune".
     */
    String object;

    /**
     * The name of the base model.
     */
    String model;

    /**
     * The creation time in epoch seconds.
     */
    @JsonProperty("created_at")
    Long createdAt;

    /**
     * List of events in this job's lifecycle. Null when getting a list of fine-tune jobs.
     */
    List<FineTuneEvent> events;

    /**
     * The ID of the fine-tuned model, null if tuning job is not finished.
     * This is the id used to call the model.
     */
    @JsonProperty("fine_tuned_model")
    String fineTunedModel;

    /**
     * The specified hyper-parameters for the tuning job.
     */
    HyperParameters hyperparams;

    /**
     * The ID of the organization this model belongs to.
     */
    @JsonProperty("organization_id")
    String organizationId;

    /**
     * Result files for this fine-tune job.
     */
    @JsonProperty("result_files")
    List<File> resultFiles;

    /**
     * The status os the fine-tune job. "pending", "succeeded", or "cancelled"
     */
    String status;

    /**
     * Training files for this fine-tune job.
     */
    @JsonProperty("training_files")
    List<File> trainingFiles;

    /**
     * The last update time in epoch seconds.
     */
    @JsonProperty("updated_at")
    Long updatedAt;

    /**
     * Validation files for this fine-tune job.
     */
    @JsonProperty("validation_files")
    List<File> validationFiles;
}
