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
package com.alibaba.agentmagic.framework.constants;

import java.util.Arrays;
import java.util.List;

public class ProcessConstant {

    public final static String HSF_EXCEPTION_ERROR_CODE = "isv.hsf-invoke-failed";

    public static final List<String> WORD_LIST = Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z");

    public static final String EXCEPTION_EVENT =  "EXCEPTION";

    public static final String CATCH_EXCEPTION_EVENT =  "CATCH_EXCEPTION";
    public static final String ASYNC_CONTEXT =  "asyncContext";

    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    public static final String DEFAULT_REMARK = "失败";
    public static final String DEFAULT_ERROR_CODE = "NodeException";

    public static final String ISP_ERROR_CODE_PREFIX = "isp.";

    public static final String ISV_ERROR_CODE_PREFIX = "isv.";

    // http节点
    public static final String ERROR_HANDLER_CONDITION_PATH = "conditionPath";
    public static final String ERROR_HANDLER_CONDITION_OPERATOR = "conditionOperator";
    public static final String ERROR_HANDLER_CONDITION_VALUE = "conditionValue";
    public static final String ERROR_HANDLER_ERROR_MESSAGE_PATH = "errorMessagePath";

    public static final String HTTP_URL = "httpUrl";

    public static final String HTTP_METHOD = "httpMethod";

    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";

    public static final String HTTP_IGNORE_SSL = "ignoreSsl";

    public static final String HTTP_BODY = "httpBody";


    public static final String HTTP_QUERY_PARAM = "httpQueryParam";

    public static final String SYSTEM_EVENT_ID = "toolSubId";
    public static final String REQUEST_TRACE_ID = "traceId";
    public static final String SYSTEM = "system";

    // 获取表单数据节点
    // 工作表id
    public static final String FORM_TABLE_CODE = "tableCode";
    // 数据空间表id
    public static final String FORM_TABLE_NAME = "tableName";
    public static final String FORM_AND_CRITERIA_LIST = "andCriteriaList";
    public static final String FORM_OR_CRITERIA_LIST = "orCriteriaList";
    public static final String FORM_ORDER_CRITERIA_LIST = "orderCriteriaList";
    public static final String FORM_COLUMN_LIST = "columnList";
    public static final String FORM_PAGE_CRITERIA = "pageCriteria";
    public static final String FORM_QUERY_ONE = "queryOne";
    public static final String FORM_QUERY_VERSION = "nodeVersion";
    public static final String FORM_QUERY_ENCRYPT = "encrypt";
    public static final String FORM_USER_ID = "userId";
    public static final String FORM_APP_KEY = "appKey";
    public static final String FORM_APPLICATION_TYPE_ENUM = "applicationTypeEnum";
    public static final String FORM_RECORD_MAP = "recordMap";
    public static final String FORM_MULTIPLE_DATA = "multipleData";
    public static final String FORM_VARIABLE = "variable";
    public static final String FORM_DATA_SOURCE = "dataSource";

    public static final String FIELD_TAOBAO_ACCOUNT_USERID = "userId";

    public static final String FIELD_ORDER_MAIN_ORDER_ID = "mainOrderId";


    // 数据展示节点
    public static final String DISPLAY_PAGE_CODE = "pageCode";
    public static final String DISPLAY_PARAM_MAP = "paramMap";
    public static final String DISPLAY_NO_DATA = "noData";
    public static final String DISPLAY_RESPONSE = "process_response";

    public static final String DISPLAY_PROCESS_INSTANCE_ID = "process_instance_id";
    public static final String DISPLAY_NEED_RETRY = "process_need_retry";
    public static final String DISPLAY_MULTIPLE_DATA = "multipleData";
    public static final String DISPLAY_VARIABLE = "variable";
    public static final String DISPLAY_DEFAULT_DATA = "defaultData";

    public static final String DISPLAY_NOT_MAPPING = "notMapping";
    public static final String DISPLAY_BIZ_SUCCESS = "bizSuccess";
    public static final String DISPLAY_BIZ_ERROR_MSG = "bizErrorMsg";

    // 连接器通用节点
    public static final String CONNECTOR_INSTANCE_ID = "connectorInstanceId";
    public static final String ACTION_ID = "actionId";
    public static final String ACTION_PARAM ="actionParam";
    public static final String CONNECTOR_NAME = "connectorName";
    public static final String ACTION_TYPE = "actionType";
    public static final String CONNECTOR_INSTANCE_CODE = "connectorInstanceCode";
    public static final String CONNECTOR_INSTANCE_ID_PRE = "connectorInstanceIdPre";
    public static final String CONNECTOR_INSTANCE_ID_ONLINE = "connectorInstanceIdOnline";
    // 连接器应用层
    public static final String CONNECTOR_APP_CODE = "appCode";
    public static final String CONNECTOR_ACTION_CODE = "actionCode";
    public static final String CONNECTOR_USER_ID = "userId";
    public static final String CONNECTOR_RETRY = "retry";
    public static final String CONNECTOR_RETRY_INTERVAL = "retryInterval";

    public static final String CONNECTOR_TEMPLATE_CODE = "templateCode";
    public static final String CONNECTOR_TEMPLATE_APPKEY = "appkey";

    // 列表排序节点
    public static final String LIST_VARIABLE_PATH = "variablePath";
    public static final String LIST_ORDER = "order";

    // 审批节点
    public static final String AUDIT_FIELD_LIST = "fieldList";
    public static final String AUDIT_USER_LIST = "userList";
    public static final String AUDIT_USER_TYPE = "userType";
    public static final String AUDIT_AUDIT_TYPE = "auditType";

    // 商家服务中心
    public static final String BUSINESS_PARAM = "param";
    public static final String BUSINESS_CONTENT = "content";
    public static final String HSF_PARAM_LIST = "paramList";

    // 计算节点
    public static final String COMPUTE_EXPRESSION = "expression";
    public static final String EXPRESSION_TYPE = "type";
    public static final String EXPRESSION_PARAM_LIST = "paramList";


    // 数据过滤节点
    public static final String FILTER_RULES = "filterRules";
    public static final String FILTER_DATA_SOURCE = "dataSource";
    public static final String FILTER_MULTIPLE_DATA = "multipleData";
    public static final String FILTER_NO_DATA_OPERATION = "noDataOperation";
    public static final String NODE_OUTPUT = "output";
    public static final String NODE_LIST_SIZE = "listSize";

    // 加锁节点
    public static final String LOCK_KEY = "key";
    public static final String LOCK_EXPIRE_TIME = "expireTime";
    public static final String LOCK_RETRY = "retry";
    public static final String LOCK_RETRY_INTERVAL = "retryInterval";

    // 列表映射节点
    public static final String LISTMAPPING_DATASOURCE = "dataSource";
    public static final String LISTMAPPING_PATH = "path";
    public static final String LISTMAPPING_MODE = "mode";

    // 列表计算节点
    public static final String LIST_CALCULATE_DATASOURCE = "dataSource";
    public static final String LIST_CALCULATE_PATH = "path";
    public static final String LIST_CALCULATE_TYPE = "calculateType";


    // 循环节点
    public static final String LOOP_DATA_SOURCE = "dataSource";
    public static final String LOOP_TOTAL_COUNT = "totalCount";

    public static final String SYS_LAST_RETRY = "lastRetry";

    // TOPAPI节点
    public static final String TOPAPI_API_CODE ="apiCode";
    public static final String TOPAPI_USER_ID ="userId";
    public static final String TOPAPI_BODY ="body";

    // 文本模板节点
    public static final String TEMPLATE_TEXT = "text";

    // 文本处理节点
    public static final String TEXT_VALUE = "text";
    public static final String TEXT_MODE = "mode";
    public static final String TEXT_PARAM = "param";

    public static final String TEXT_PARAM_SOURCE = "source";

    public static final String TEXT_PARAM_TARGET = "target";


    public static final String PROCESS_VALUE = "source";

    public static final String PROCESS_VALUE_DEFINE = "define";

    public static final String PROCESS_VALUE_TYPE = "type";

    public static final String PROCESS_VALUE_TARGET = "target";

    // 审批
    public static final String USER_CONFIG = "userConfig";

    public static final String USER_CONFIG_TYPE = "type";
    public static final String USER_CONFIG_USER_LIST = "userIdList";
    public static final String USER_TASK_GROUP_CONFIG = "taskGroupConfig";
    public static final String USER_TASK_CONTEXT = "context";

    public static final String AUDIT_STATUS = "audit_status";

    public static final String AUDIT_REMARK = "audit_remark";
    public static final String AUDIT_LATEST_OPERATOR = "audit_latest_operator";
    public static final String AUDIT_ALLOW_TRANSFER = "allowTransfer";

    public static final String DELAY_TYPE_FIXED = "fixedInterval";
    public static final String DELAY_TYPE = "delayType";
    public static final String DELAY_TIME_NUM = "timeNum";
    public static final String DELAY_TIME_UNIT = "timeUnit";
    public static final String TIME_UNIT_DAY = "day";
    public static final String TIME_UNIT_HOUR = "hour";
    public static final String TIME_UNIT_MINUTE = "minute";
    public static final String TIME_UNIT_SECOND = "second";
    public static final String EVENT_APPKEY = "34023711";

    public static final String PARAM_TYPE_STRING = "STRING";

    public static final String ENV_ONLINE = "online";

    public static final String SYSTEM_USER_ID_KEY = "$!{system.user}";
    public static final String SYSTEM_APP_ID_KEY = "$!{system.appId}";
    public static final String RESULT_FUTURE = "resultFuture";
    public static final String DELAY_NODE_TOPIC = "taobao_smartapp_FlowDelay";
    public static final String ASYNC_NODE_TIMEOUT_TOPIC = "taobao_smartapp_FlowNodeTimeout";

    public static final String DATE_ORIGIN_TIME_LIST = "originTimeList";
    public static final String DATE_ORIGIN_DATE_FORMAT = "originDateFormat";
    public static final String DATE_TARGET_DATE_FORMAT = "targetDateFormat";
    public static final String DATE_TARGET_FORMAT = "targetFormat";

    // 抛出异常节点
    public static final String INTERRUPT_ERROR_MSG = "errorMsg";
    public static final String INTERRUPT_ERROR_CODE = "errorCode";
    public static final String INTERRUPT_REMARK = "remark";

    //
    public static final Integer PROCESS_RUNNING = 1;

    // 固定输出节点
    public static final String MESSAGE_STRING = "message";
    public static final String COMMAND_STRING = "command";

    // 工具调用节点

    // 消息节点
    public static final String MESSAGE_NODE_OUT_PUT_KEY = "message";
}
