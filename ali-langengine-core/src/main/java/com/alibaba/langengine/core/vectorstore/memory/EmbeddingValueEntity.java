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
package com.alibaba.langengine.core.vectorstore.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmbeddingValueEntity {

    private final Double[] vector;

    public EmbeddingValueEntity(Double[] vector) {
        this.vector = vector;
    }

    public Double[] vector() {
        return vector;
    }

    public List<Double> vectorAsList() {
        List<Double> list = new ArrayList<>(vector.length);
        for (Double f : vector) {
            list.add(f);
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmbeddingValueEntity that = (EmbeddingValueEntity) o;
        return Arrays.equals(this.vector, that.vector);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vector);
    }

    @Override
    public String toString() {
        return "EmbeddingValueEntity {" +
                " vector = " + Arrays.toString(vector) +
                " }";
    }

    public static EmbeddingValueEntity from(Double[] vector) {
        return new EmbeddingValueEntity(vector);
    }

    public static EmbeddingValueEntity from(List<Double> vector) {
        Double[] array = new Double[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            array[i] = vector.get(i);
        }
        return new EmbeddingValueEntity(array);
    }
}
