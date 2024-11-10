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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Memory for the Tree of Thought (ToT) chain.
 * Implemented as a stack of thoughts.
 * This allows for a depth first search (DFS) of the ToT.
 * 思想树（ToT）链的内存。
 * 作为思想的堆栈来实现。
 * 这允许对 ToT 进行深度优先搜索 (DFS)。
 *
 * @author xiaoxuan.lp
 */
public class ToTDFSMemory {

    private Stack<Thought> stack;

    public ToTDFSMemory() {
        stack = new Stack<>();
    }

    public Thought top() {
        if (!stack.isEmpty()) {
            return stack.peek();
        }
        return null;
    }

    public Thought pop() {
        return pop(1);
    }

    public Thought pop(int n) {
        if (stack.size() < n) {
            return null;
        }
        Thought thought = null;
        for (int i = 0; i < n; i++) {
            thought = stack.pop();
        }
        return thought;
    }

    public Thought topParent() {
        if (stack.size() > 1) {
            return stack.get(stack.size() - 2);
        }
        return null;
    }

    public void store(Thought node) {
        if (!stack.isEmpty()) {
            stack.peek().getChildren().add(node);
        }
        stack.push(node);
    }

    public int getLevel() {
        return stack.size();
    }

    public List<Thought> getCurrentPath() {
        return new ArrayList<>(stack);
    }
}
