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
package com.alibaba.langengine.agentframework.model.enums;

/**
 * 错误码枚举
 *
 * @author xiaoxuan.lp
 */
public enum AgentMagicErrorCode {

    INVALID_REQUEST("isv.invalid-request", "非法请求"),

    SYSTEM_ERROR("isp.system-error", "系统出错了"),
    UNKNOWN_SYSTEM_ERROR("isp.unknown-system-error", "未知的系统错误"),

    PIPE_SYSTEM_ERROR("isp.pipe-system-error", "pipe出错了"),

    TOOL_SYSTESM_ERROR("500", "isp.tool-system-error"),
    TOOL_ASYNC_CALL_TIME_OUT("503", "isp.tool-time-out"),
    TOOL_APIGATEWAY_ERROR("500", "isp.tool-apigateway-error"),
    MESSAGE_SYSTEM_ERROR("500", "isp.message-system-error"),

    PROCESS_INVALID_RESPONSE("isp.process-invalid-response", "非法的流程响应"),
    PROCESS_START_ERROR("500", "isp.process-start-error"),
    PROCESS_INTERRUPT("isp.process-interrupt", "流程中断"),
    PROCESS_SIGNAL_ERROR("isp.process-signal-error", "流程恢复失败"),

    PROCESS_DEFINITION_NOT_EXIST("isp.processdef-not-exist", "流程定义不存在"),
    PROCESS_DEFINITION_PARSE_ERROR("isp.processdef-parse-error", "流程定义解析出错"),
    PROCESS_DEFINITION_CREATE_ERROR("isp.processdef-create-error", "流程定义创建出错"),

    PROCESS_INSTANCE_NOT_EXIST("isp.procinst-not-exist", "流程实例不存在"),
    PROCESS_INSTANCE_ACTIVIY_NOT_EXIST("isp.activity-not-exist", "流程节点不存在"),
    PROCESS_INSTANCE_NOT_PARSE("isp.procinst-not-parse", "流程实例Flow解析出错"),

    RETRIEVAL_SEARCH_ERROR("isp.retrieval-search-error", "检索查询出错"),
    EMBEDDING_ERROR("isp.embedding-error", "Embedding出错"),

    LLM_SYSTEM_ERROR("isp.llm-system-error", "LLM系统异常"),
    LLM_FETCH_ERROR("isp.llm-fetch-error", "获取LLM出错"),

    INTENT_SYSTEM_ERROR("500", "isp.intent-system-error"),

    COT_SYSTEM_ERROR("500", "isp.cot-system-error"),

    SCRIPT_EVALUATE_ERROR("500", "isp.script-evaluate-error"),
    CODE_EXECUTION_ERROR("500", "isp.code-execution-error"),
    DYNAMIC_SCRIPT_PY_CODE_ERROR("500", "isp.dynamic-script-py-code-error"),
    DYNAMIC_SCRIPT_GROOVY_CODE_ERROR("500", "isp.dynamic-script-groovy-code-error"),


    APIKEY_QUERY_FROM_DB_ERROR("isp.apikey-query-from-db-error", "从数据库获取apikey出错了"),
    APIKEY_QUERY_FROM_DB_IS_EMPTY("isp.apikey-query-from-db-is-empty", "从数据库获取apikey为空"),
    APIKEY_NOT_FOUND_ANYWHERE("isp.apikey-not-found-anywhere", "找不到apikey"),

    PARALLEL_NODE_PROCESS("parallel_node_process", "parallel_node_process"),
    PARALLEL_NODE_ERROR("isp.parallel-node-error", "并行节点中有节点异常，或者没有以\"并行节点\"作为结尾"),

    INPUT_PARAMETER_ERROR("isp.input-parameter-error", "输入参数错误"),
    ;

    AgentMagicErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public final String code;
    public final String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
