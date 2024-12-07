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
package com.alibaba.langengine.core.jtokkit.api;

import java.util.Optional;

/**
 * The EncodingRegistry is used to register custom encodings and to retrieve
 * encodings by name or type. The out-of-the-box supported encodings are registered automatically.
 */
public interface EncodingRegistry {

    /**
     * Returns the encoding with the given name, if it exists. Otherwise, returns an empty Optional.
     * built-in encodings.
     *
     * @param encodingName the name of the encoding
     * @return the encoding, if it exists
     */
    Optional<Encoding> getEncoding(String encodingName);

    /**
     * Returns the encoding with the given type.
     *
     * @param encodingType the type of the encoding
     * @return the encoding
     */
    Encoding getEncoding(EncodingType encodingType);

    /**
     * Returns the encoding that is used for the given model type, if it exists. Otherwise, returns an
     * <p>
     * Note that you can use this method to retrieve the correct encodings for snapshots of models, for
     * example "gpt-4-0314" or "gpt-3.5-turbo-0301".
     *
     * @param modelName the name of the model to get the encoding for
     * @return the encoding, if it exists
     */
    Optional<Encoding> getEncodingForModel(String modelName);

    /**
     * Returns the encoding that is used for the given model type.
     *
     * @param modelType the model type
     * @return the encoding
     */
    Encoding getEncodingForModel(ModelType modelType);

    /**
     * Registers a new byte pair encoding with the given name. The encoding must be thread-safe.
     *
     * @param parameters the parameters for the encoding
     * @return the registry for method chaining
     * @see GptBytePairEncodingParams
     * @throws IllegalArgumentException if the encoding name is already registered
     */
    EncodingRegistry registerGptBytePairEncoding(GptBytePairEncodingParams parameters);

    /**
     * Registers a new custom encoding with the given name. The encoding must be thread-safe.
     *
     * @param encoding the encoding
     * @return the registry for method chaining
     * @throws IllegalArgumentException if the encoding name is already registered
     */
    EncodingRegistry registerCustomEncoding(Encoding encoding);
}
