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
package com.alibaba.langengine.agentframework.model.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 流程系统参数
 */
@Data
public class ProcessSystemContext implements Serializable {

    public final static String KEY = "processSystemContext";

    /**
     * 是否存在持久化，动态修改
     */
    Boolean hasRecord = false;

    /**
     * 当前重试次数
     */
    Integer retryTimes = 0;

    /**
     * 是否异步执行了该节点
     */
    Map<String, Boolean> nodeCallback = new ConcurrentHashMap<>();

    /**
     * 是否异步执行了子节点
     */
    Map<String, Boolean> nodeSubCallback = new ConcurrentHashMap<>();

    /**
     * 节点重试次数
     */
    Map<String,Integer> nodeRetry = new ConcurrentHashMap<>();

    /**
     * 当前工作流实例id
     */
    String processInstanceId;

    /**
     * 流程状态
     */
    String processStatus;

    /**
     * 流程中锁列表
     */
    List<String> lockKeyList = new ArrayList<>();

    /**
     * 自动化模板，是否需要写入运行记录表
     */
    Boolean needInsertLog = false;


    /**
     * 审批流使用，保存最近一次审批节点审批情况
     */
//    ProcessAuditContext auditContext;

    public void putCallbackNode(String activityId) {
        nodeCallback.put(activityId, true);
    }

    public void clearCallbackNode() {
        nodeCallback.clear();
    }

    public void clearCallbackNode(String activityId) {
        nodeCallback.put(activityId,false);
    }

    public void putSubCallbackNode(String activityId) {
        nodeSubCallback.put(activityId, true);
    }

    public void addLockKey(String lockKey) {
        if(lockKeyList != null) {
            lockKeyList.add(lockKey);
        }
    }

    /**
     * 获取重试次数
     * @param activityId
     * @return
     */
    public Integer getNodeRetryTimes(String activityId) {
        return nodeRetry.getOrDefault(activityId, 0);
    }

    /**
     * 清空重试次数
     * @param activityId
     * @return
     */
    public void clearRetryTimes(String activityId) {
        nodeRetry.put(activityId,0);
    }

    /**
     * 清空重试次数
     * @return
     */
    public void clearRetryTimes() {
        nodeRetry.clear();
    }

    /**
     * 重试次数加一
     * @param activityId
     */
    public void addRetryTimes(String activityId) {
        Integer retryTimes = nodeRetry.getOrDefault(activityId, 0);
        nodeRetry.put(activityId,retryTimes+1);
    }

}
