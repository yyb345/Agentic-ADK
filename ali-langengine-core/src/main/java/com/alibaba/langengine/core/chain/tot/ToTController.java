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
package com.alibaba.langengine.core.chain.tot;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree of Thought (ToT) controller.
 * This is a version of a ToT controller, dubbed in the paper as a "Simple Controller".
 * It has one parameter `c` which is the number of children to explore for each thought.
 *
 * @author xiaoxuan.lp
 */
@Data
public class ToTController {

    private int c;

    public ToTController() {
        this(3);
    }

    public ToTController(int c) {
        this.c = c;
    }

    public List<String> call(ToTDFSMemory memory) {
        Thought nextThought = memory.top();
        Thought parentThought = memory.topParent();
        ThoughtValidity validity = (nextThought == null) ? ThoughtValidity.VALID_INTERMEDIATE : nextThought.getValidity();

        if (validity == ThoughtValidity.INVALID) {
            memory.pop();
            nextThought = memory.top();
            if (nextThought != null && nextThought.getChildren().size() >= c) {
                memory.pop();
            }
        } else if (validity == ThoughtValidity.VALID_INTERMEDIATE && parentThought != null && parentThought.getChildren().size() >= c) {
            memory.pop(2);
        }

        List<Thought> currentPath = memory.getCurrentPath();
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < currentPath.size(); i++) {
            texts.add(currentPath.get(i).getText());
        }
        return texts;
    }
}
