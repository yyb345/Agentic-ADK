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
package com.alibaba.langengine.gpt.nl2sql.db;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.langengine.gpt.nl2sql.db.PromptConstants.QUERY_CHECKER_PROMPT_TEMPLATE_EN;

/**
 * Chain for interacting with SQL Database.
 * 用于与SQL数据库交互的链。
 *
 * @author xiaoxuan.lp
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class SQLDatabaseChain extends Chain {

    /**
     * llm chain
     */
    private LLMChain llmChain;

    /**
     * SQL Database to connect to
     */
    private SQLDatabase database;

    /**
     * Number of results to return from the query
     */
    private Integer topK = 5;

    private String inputKey = "query";

    private String outputKey = "result";

    /**
     * Will return sql-command directly without executing it
     * 会直接返回sql-command而不执行它
     */
    private Boolean returnSql = false;

    /**
     * Whether or not to return the intermediate steps along with the final answer.
     */
    private Boolean returnIntermediateSteps = false;

    /**
     * Whether or not to return the result of querying the SQL table directly.
     */
    private Boolean returnDirect = false;

    /**
     * Whether or not the query checker tool should be used to attempt
     * to fix the initial SQL from the LLM.
     */
    private Boolean useQueryChecker = false;

    /**
     * The prompt template that should be used by the query checker
     */
    private BasePromptTemplate queryCheckerPrompt;

    private static final String INTERMEDIATE_STEPS_KEY = "intermediate_steps";

    public static SQLDatabaseChain fromLlm(BaseLanguageModel llm, SQLDatabase database) {
        return fromLlm(llm, database, null);
    }

    public static SQLDatabaseChain fromLlm(BaseLanguageModel llm, SQLDatabase database, BasePromptTemplate prompt){
        if(prompt == null) {
//            prompt = PromptConstants.SQLITE_PROMPT_TEMPLATE_EN;
            prompt = PromptConstants.DEFAULT_PROMPT_TEMPLATE_EN;
        }
        LLMChain chain = new LLMChain();
        chain.setLlm(llm);
        chain.setPrompt(prompt);
        SQLDatabaseChain sqlDatabaseChain = new SQLDatabaseChain();
        sqlDatabaseChain.setLlmChain(chain);
        sqlDatabaseChain.setDatabase(database);
        return sqlDatabaseChain;
    }

    @Override
    public Map<String, Object> call(Map<String, Object> inputs, ExecutionContext executionContext, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        String inputText = String.format("%s\nSQLQuery:", inputs.get(inputKey));
        String tableInfo = getDatabase().getTableInfo();

        Map<String, Object> llmInputs = new HashMap<>();
        llmInputs.put("input", inputText);
        llmInputs.put("top_k", topK.toString());
        llmInputs.put("dialect", database.getEngine().getDialect());
        llmInputs.put("table_info", tableInfo);
        llmInputs.put("stop", Arrays.asList(new String[] { "\nSQLResult:"} ));

        List<Object> intermediateSteps = new ArrayList<>();
        intermediateSteps.add(llmInputs);

        Map<String, Object> result = llmChain.run(llmInputs);

        String sqlCmd = ((String) result.get("text")).trim();
        //为了适配通义千问
        String regex = "(.*?)SQLQuery:(.*?)$";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sqlCmd);
        if (matcher.find()) {
            sqlCmd = matcher.group(2).trim().replaceAll("\n", " ");
        }
        log.warn("sqlCmd:" + sqlCmd);
        intermediateSteps.add(sqlCmd);

        if(useQueryChecker) {
            if(queryCheckerPrompt == null) {
                queryCheckerPrompt = QUERY_CHECKER_PROMPT_TEMPLATE_EN;
            }
            LLMChain queryCheckerChain = new LLMChain();
            queryCheckerChain.setLlm(llmChain.getLlm());
            queryCheckerChain.setPrompt(queryCheckerPrompt);

            Map<String, Object> queryCheckerInputs = new HashMap<>();
            queryCheckerInputs.put("query", sqlCmd);
            queryCheckerInputs.put("dialect", database.getEngine().getDialect());

            Map<String, Object> checkedSqlResult = queryCheckerChain.run(queryCheckerInputs, extraAttributes);
            sqlCmd = ((String) checkedSqlResult.get("text")).trim();
            log.warn("checkedSqlCmd:" + sqlCmd);
            intermediateSteps.add(sqlCmd);
        }

        if(returnSql) {
            Map<String, Object> outputs = new HashMap<>();
            outputs.put(outputKey, sqlCmd);
            return outputs;
        }

        List<List<String>> sqlCmdResult = null;
        String finalResult;

        Map<String, Object> sqlCmdMap = new HashMap<>();
        sqlCmdMap.put("sql_cmd", sqlCmd);
        intermediateSteps.add(sqlCmdMap);

        try {
            ResultSet resultSet = getDatabase().executeQuery(sqlCmd);
            sqlCmdResult = SQLEngine.resultSetToTable(resultSet);
            String sqlCmdResultStr = JSON.toJSONString(sqlCmdResult);
            log.warn("sqlCmdResult:" + sqlCmdResultStr);
            intermediateSteps.add(sqlCmdResultStr);
            resultSet.close();
        } catch (Exception e) {
            log.error("executeQuery error", e);
        }

        if(returnDirect) {
            finalResult = JSON.toJSONString(sqlCmdResult);
        } else {
            inputText += String.format("%s\nSQLResult: %s\nAnswer:", sqlCmd, JSON.toJSONString(sqlCmdResult));
            llmInputs.put("input", inputText);
            intermediateSteps.add(llmInputs);

            result = llmChain.run(llmInputs);
            finalResult = ((String) result.get("text")).trim();
            intermediateSteps.add(finalResult);
        }

        Map<String, Object> chainResult = new HashMap<>();
        chainResult.put(outputKey, finalResult);
        if(returnIntermediateSteps) {
            chainResult.put("intermediate_steps", intermediateSteps);
        }
        return result;
    }

    @Override
    public List<String> getInputKeys() {
        return Arrays.asList(new String[]{ inputKey });
    }

    @Override
    public List<String> getOutputKeys() {
        if(!returnIntermediateSteps) {
            return Arrays.asList(new String[]{ outputKey });
        } else {
            return Arrays.asList(new String[]{ outputKey, INTERMEDIATE_STEPS_KEY });
        }
    }
}
