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
package com.alibaba.langengine.core.callback;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.model.CallBackTraceDTO;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.indexes.BaseRetriever;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.tool.BaseTool;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author aihe.ah
 * @time 2023/11/22
 * 功能说明：
 * 1、如果在回调中使用了这个类，那么就会把回调的内容打印到本地日志中，方便排查问题
 * 2、在SLS中配置JSON模式，就可以进行JSON化展示；
 * - 当前集团的应用，在SLS中可以直接添加对应的机器进行采集，相对比较简单；
 * 3、配置引入方式，在logback.xml中配置:
 * <include resource="com/alibaba/langengine/core/callback/langengine-trace.xml" />
 * 4、在代码中回调管理器加上这个类即可
 */
@Slf4j
public class LocalLogCallBackHandler extends BaseCallbackHandler {

    private static final Logger TRACE_LOG = LoggerFactory.getLogger("llm_trace_log");

    @Override
    public void onChainStart(ExecutionContext executionContext) {
        logBiz("onChainStart", executionContext);

    }

    private void logBiz(String stage, ExecutionContext executionContext) {
        try {
            CallBackTraceDTO traceDTO = new CallBackTraceDTO();
            traceDTO.setTraceId(getTraceId());
            traceDTO.setStage(stage);
            copyObj(executionContext, traceDTO);
            if (executionContext instanceof BizExecutionContext) {
                traceDTO.setBizContext(((BizExecutionContext)executionContext).getBizContext());
                traceDTO.setUserId(((BizExecutionContext)executionContext).getUserId());
                traceDTO.setUtBizParams(((BizExecutionContext)executionContext).getUtBizParams());
                traceDTO.setSessionId(((BizExecutionContext)executionContext).getSessionId());
                traceDTO.setBizId(((BizExecutionContext)executionContext).getBizId());
            }

            traceDTO.setChainInfo(extractChainInfo(executionContext.getChain()));
            traceDTO.setChildChainInfo(extractChainInfo(executionContext.getChildChain()));
            traceDTO.setLlmInfo(extractLlmInfo(executionContext.getLlm()));
            traceDTO.setRetrieverInfo(extractRetrieverInfo(executionContext.getRetriever()));
            TRACE_LOG.info(JSON.toJSONString(traceDTO));
        } catch (Exception e) {
            log.error("logBiz error", e);
        }

    }

    /**
     * 把executionContext中的内容拷贝到traceDTO中
     *
     * @param executionContext
     * @param traceDTO
     */
    private void copyObj(ExecutionContext executionContext, CallBackTraceDTO traceDTO) {
        traceDTO.setChainInstanceId(executionContext.getChainInstanceId());
        traceDTO.setEagleEyeCtx(executionContext.getEagleEyeCtx());
        traceDTO.setThrowable(executionContext.getThrowable());

        traceDTO.setInputs(executionContext.getInputs());
        traceDTO.setOutputs(executionContext.getOutputs());
        traceDTO.setPrompts(executionContext.getPrompts());
        traceDTO.setLlmResult(executionContext.getLlmResult());

        traceDTO.setChildInputs(executionContext.getChildInputs());
        traceDTO.setChildOutputs(executionContext.getChildOutputs());
        traceDTO.setToolInfo(extractToolInfo(executionContext.getTool()));
        traceDTO.setToolInput(executionContext.getToolInput());
        traceDTO.setToolExecuteResult(executionContext.getToolExecuteResult());

        traceDTO.setExecutionType(executionContext.getExecutionType());
        traceDTO.setToolExecuteResult(executionContext.getToolExecuteResult());

        traceDTO.setAgentAction(executionContext.getAgentAction());
        traceDTO.setAgentFinish(executionContext.getAgentFinish());

        traceDTO.setRetrieverInput(executionContext.getRetrieverInput());
        traceDTO.setRetrieverOutput(executionContext.getRetrieverOutput());
    }

    private Map<String, Object> extractToolInfo(BaseTool tool) {
        if (tool == null) {
            return null;
        }
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("toolName", tool.getName());
        hashMap.put("description", tool.getDescription());
        hashMap.put("functionName", tool.getFunctionName());
        hashMap.put("args", tool.getArgs());
        hashMap.put("returnDirect", tool.isReturnDirect());
        hashMap.put("verbose", tool.isVerbose());

        return hashMap;
    }

    private Map<String, Object> extractLlmInfo(BaseLanguageModel llm) {
        if (llm == null) {
            return null;
        }
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("model", llm.getTraceInfo());
        return hashMap;
    }

    private Map<String, Object> extractChainInfo(Chain chain) {
        if (chain == null) {
            return null;
        }
        Map<String, Object> chainInfo = new HashMap<>();
        chainInfo.put("chain", chain.toString());
        if(chain.getMemory() != null) {
            chainInfo.put("memory", chain.getMemory().getClass().getName());
        }
        return chainInfo;
    }

    private Map<String, Object> extractRetrieverInfo(BaseRetriever retriever) {
        if (retriever == null) {
            return null;
        }
        Map<String, Object> retrieverInfo = new HashMap<>();
        retrieverInfo.put("retriever", retriever.toString());
        return retrieverInfo;
    }

    @Override
    public void onChainEnd(ExecutionContext executionContext) {
        logBiz("onChainEnd", executionContext);
    }

    @Override
    public void onChainError(ExecutionContext executionContext) {
        logBiz("onChainError", executionContext);
    }

    @Override
    public void onLlmStart(ExecutionContext executionContext) {
        logBiz("onLlmStart", executionContext);
    }

    @Override
    public void onLlmEnd(ExecutionContext executionContext) {
        logBiz("onLlmEnd", executionContext);
    }

    @Override
    public void onLlmError(ExecutionContext executionContext) {
        logBiz("onLlmError", executionContext);
    }

    @Override
    public void onToolStart(ExecutionContext executionContext) {
        logBiz("onToolStart", executionContext);
    }

    @Override
    public void onToolEnd(ExecutionContext executionContext) {
        logBiz("onToolEnd", executionContext);
    }

    @Override
    public void onToolError(ExecutionContext executionContext) {
        logBiz("onToolError", executionContext);
    }

    @Override
    public void onAgentAction(ExecutionContext executionContext) {
        logBiz("onAgentAction", executionContext);
    }

    @Override
    public void onAgentFinish(ExecutionContext executionContext) {
        logBiz("onAgentFinish", executionContext);
    }

    @Override
    public void onRetrieverStart(ExecutionContext executionContext) {
        logBiz("onRetrieverStart", executionContext);
    }

    @Override
    public void onRetrieverEnd(ExecutionContext executionContext) {
        logBiz("onRetrieverEnd", executionContext);
    }

    @Override
    public void onRetrieverError(ExecutionContext executionContext) {
        logBiz("onRetrieverError", executionContext);
    }
}
