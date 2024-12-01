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
package com.alibaba.agentmagic.framework.config.extension;

import com.alibaba.smart.framework.engine.common.util.IdAndVersionUtil;
import com.alibaba.smart.framework.engine.configuration.ProcessEngineConfiguration;
import com.alibaba.smart.framework.engine.configuration.aware.ProcessEngineConfigurationAware;
import com.alibaba.smart.framework.engine.deployment.ProcessDefinitionContainer;
import com.alibaba.smart.framework.engine.extension.annoation.ExtensionBinding;
import com.alibaba.smart.framework.engine.extension.constant.ExtensionConstant;
import com.alibaba.smart.framework.engine.model.assembly.ProcessDefinition;
import com.alibaba.smart.framework.engine.pvm.PvmProcessDefinition;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 扩展流程定义容器
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@ExtensionBinding(group = ExtensionConstant.SERVICE, bindKey = ProcessDefinitionContainer.class,priority = 1)
public class CacheProcessDefinitionContainer implements ProcessDefinitionContainer, ProcessEngineConfigurationAware {

    private ProcessEngineConfiguration processEngineConfiguration;

    /**
     * 使用Caffeine代替ConcurrentHashMap
     */
    private static CaffeineCache processDefinitionCache =  new CaffeineCache("processDefinitionCache",
            Caffeine.newBuilder()
                    .recordStats()
                    .expireAfterAccess(3600L,TimeUnit.SECONDS)
                    .maximumSize(20000).build());

    @Override
    public void setProcessEngineConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    @Override
    public Map<String, PvmProcessDefinition> getPvmProcessDefinitionConcurrentHashMap() {
        Map<String, PvmProcessDefinition> collect = processDefinitionCache.getNativeCache().asMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                        e -> (PvmProcessDefinition) ((Pair) e.getValue()).getKey()));
        return collect;
    }

    @Override
    public Map<String, ProcessDefinition> getProcessDefinitionConcurrentHashMap() {
        Map<String, ProcessDefinition> collect = processDefinitionCache.getNativeCache().asMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                        e -> (ProcessDefinition) ((Pair) e.getValue()).getValue()));
        return collect;
    }

    @Override
    public void install(PvmProcessDefinition pvmProcessDefinition, ProcessDefinition processDefinition) {
        String processDefinitionId = pvmProcessDefinition.getId();
        String version = pvmProcessDefinition.getVersion();
        String uniqueKey = IdAndVersionUtil.buildProcessDefinitionKey(processDefinitionId, version);

        log.info("Cache install processDefinitionId:" + processDefinitionId + ", version:" + version + ", uniqueKey:" + uniqueKey);

        Pair pair = processDefinitionCache.get(uniqueKey, Pair.class);
        if(null != pair){
            log.warn(" Duplicated processDefinitionId and version found for unique key "+uniqueKey+" , but it's ok for deploy the process definition repeatedly. BUT this message should be NOTICED. ");
        }
        Pair<PvmProcessDefinition,ProcessDefinition> definitionPair = new ImmutablePair<>(pvmProcessDefinition,processDefinition);
        processDefinitionCache.put(uniqueKey,definitionPair);
    }

    @Override
    public void uninstall(String processDefinitionId, String version) {
        String uniqueKey = IdAndVersionUtil.buildProcessDefinitionKey(processDefinitionId, version);
        log.info("Cache uninstall processDefinitionId:" + processDefinitionId + ", version:" + version + ", uniqueKey:" + uniqueKey);
        processDefinitionCache.evict(uniqueKey);
    }

    @Override
    public PvmProcessDefinition getPvmProcessDefinition(String processDefinitionId, String version) {
        String uniqueKey = IdAndVersionUtil.buildProcessDefinitionKey(processDefinitionId, version);
        return this.getPvmProcessDefinition(uniqueKey);
    }

    @Override
    public PvmProcessDefinition getPvmProcessDefinition(String uri) {
        Pair pair = processDefinitionCache.get(uri, Pair.class);
        if(pair == null) {
            return null;
        }
        return (PvmProcessDefinition)pair.getKey();
    }

    @Override
    public ProcessDefinition getProcessDefinition(String processDefinitionId, String version) {
        String uniqueKey = IdAndVersionUtil.buildProcessDefinitionKey(processDefinitionId, version);
        return this.getProcessDefinition(uniqueKey);
    }

    @Override
    public ProcessDefinition getProcessDefinition(String uri) {
        Pair pair = processDefinitionCache.get(uri, Pair.class);
        if(pair == null) {
            return null;
        }
        return (ProcessDefinition)pair.getValue();
    }
}
