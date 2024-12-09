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
package com.alibaba.langengine.metagpt.memory;

import com.alibaba.langengine.metagpt.Message;
import com.alibaba.langengine.metagpt.actions.Action;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The most basic memory: super-memory
 *
 * @author xiaoxuan.lp
 */
@Data
public class Memory {

    private List<Message> storage = new ArrayList<>();

    private Map<Class<? extends Action>, List<Message>> index = new HashMap<>();

    public void add(Message message) {
        if (storage.contains(message)) {
            return;
        }
        storage.add(message);
        if (message.getCauseBy() != null) {
            index.computeIfAbsent(message.getCauseBy(), k -> new ArrayList<>()).add(message);
        }
    }

    public void addBatch(Iterable<Message> messages) {
        for (Message message : messages) {
            add(message);
        }
    }

    public List<Message> getByRole(String role) {
        return storage.stream()
                .filter(message -> message.getRole().equals(role))
                .collect(Collectors.toList());
    }

    public List<Message> getByContent(String content) {
        return storage.stream()
                .filter(message -> message.getContent() != null && message.getContent().contains(content))
                .collect(Collectors.toList());
    }

    public void delete(Message message) {
        storage.remove(message);
        if (message.getCauseBy() != null) {
            index.get(message.getCauseBy()).remove(message);
        }
    }

    public void clear() {
        storage.clear();
        index.clear();
    }

    public int count() {
        return storage.size();
    }

    public List<Message> tryRemember(String keyword) {
        return storage.stream()
                .filter(message -> message.getContent() != null && message.getContent().contains(keyword))
                .collect(Collectors.toList());
    }

    public List<Message> get(int k) {
        if (k == 0) {
            return storage;
        }
        int startIndex = Math.max(storage.size() - k, 0);
        return storage.subList(startIndex, storage.size());
    }

    public List<Message> findNews(List<Message> observed, int k) {
        List<Message> alreadyObserved = get(k);
        List<Message> news = new ArrayList<>();
        for (Message message : observed) {
            if (alreadyObserved.contains(message)) {
                continue;
            }
            news.add(message);
        }
        return news;
    }

    public List<Message> getByAction(Class<? extends Action> action) {
        return index.getOrDefault(action, new ArrayList<>());
    }

    public List<Message> getByActions(Iterable<Class<? extends Action>> actions) {
        List<Message> rsp = new ArrayList<>();
        for (Class<? extends Action> action : actions) {
            List<Message> messages = index.get(action);
            if (messages != null) {
                rsp.addAll(messages);
            }
        }
        return rsp;
    }
}
