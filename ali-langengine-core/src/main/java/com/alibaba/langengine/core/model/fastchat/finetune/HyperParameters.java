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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Fine-tuning job hyperparameters
 *
 * https://beta.openai.com/docs/api-reference/fine-tunes
 */
@Data
public class HyperParameters {

    /**
     * The batch size to use for training.
     */
    @JsonProperty("batch_size")
    Integer batchSize;

    /**
     * The learning rate multiplier to use for training.
     */
    @JsonProperty("learning_rate_multiplier")
    Double learningRateMultiplier;

    /**
     * The number of epochs to train the model for.
     */
    @JsonProperty("n_epochs")
    Integer nEpochs;

    /**
     * The weight to use for loss on the prompt tokens.
     */
    @JsonProperty("prompt_loss_weight")
    Double promptLossWeight;
}
