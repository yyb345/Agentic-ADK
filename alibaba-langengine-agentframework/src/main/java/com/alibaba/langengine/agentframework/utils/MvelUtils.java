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
package com.alibaba.langengine.agentframework.utils;

import com.alibaba.langengine.agentframework.model.enums.OperatorEnum;
import com.alibaba.smart.framework.engine.common.util.StringUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.ast.ASTNode;
import org.mvel2.ast.BinaryOperation;
import org.mvel2.compiler.ExecutableAccessor;
import org.springframework.cache.caffeine.CaffeineCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Mvel表达式解析
 *
 * @author xiaoxuan.lp
 */
public class MvelUtils {

    /**
     *  表达式缓存，key是表达式字符串，value是编译后的表达式。
     */
    private static CaffeineCache EXP_CACHE =  new CaffeineCache("expCache",
            Caffeine.newBuilder()
                    .recordStats()
                    .expireAfterAccess(30L, TimeUnit.SECONDS)
                    .maximumSize(10000).build());


    private static final String START_TAG = "${";
    private static final String END_TAG = "}";

    private static ParserContext parserContext = new ParserContext();
    static {
        for (OperatorEnum operator : OperatorEnum.values()) {
            parserContext.addImport(operator.getFuncName(),MVEL.getStaticMethod(UserFunction.class,operator.getFuncName(),operator.getParamType()));
        }
    }

    public static Object eval(String expression, Map<String, Object> vars, boolean needCached) {
        //编译表达式
        Serializable compiledExp = compileExp(expression,needCached);
        //执行表达式
        return MVEL.executeExpression(compiledExp, vars);
    }

    /**
     * 编译表达式。
     *
     * @param expression 表达式字符串
     * @return 编译后的表达式字符串
     */
    private static Serializable compileExp(String expression,boolean needCached) {
        String processedExp = expression.trim();

        // 兼容Activiti ${nrOfCompletedInstances >= 1} 这种 JUEL 表达式;通过下面的调用去掉首尾.

        if(processedExp.startsWith(START_TAG)){
            processedExp =  StringUtil.removeStart(processedExp, START_TAG);
            processedExp =  StringUtil.removeEnd(processedExp, END_TAG);
        }


        //首先从缓存里取，取不到则新编译。
        Serializable compiledExp = EXP_CACHE.get(processedExp,Serializable.class);

        if (null == compiledExp) {
            compiledExp = MVEL.compileExpression(processedExp,parserContext);
            // cache 缓存结果

            if(needCached){
                EXP_CACHE.put(processedExp, compiledExp);
            }
        }
        return compiledExp;
    }

    public static Number getRightValueForBinaryOperationExpression(String expression) {
        Serializable serializable= compileExp(expression,true);
        ExecutableAccessor executableAccessor = (ExecutableAccessor)serializable;
        BinaryOperation binaryOperation = (BinaryOperation) executableAccessor.getNode();
        ASTNode right = binaryOperation.getRight();
        Number rightValue = (Number)right.getLiteralValue();
        return rightValue;
    }

    public static void main(String[] args) {
        Map<String,Object> inputData = new HashMap<>();
        List<String> array = new ArrayList<>();
        array.add("1");
        array.add("2");
        inputData.put("aaa", array);
        inputData.put("receiver","明天a");
        Map<String,Object> param = new HashMap<>();
        param.put("inputData",inputData);
        Object eval = eval("return ['res':inputData.receiver ~= '[一-龥]+'];", param, true);
        System.out.println(String.valueOf(eval));
        System.out.println(inputData);
    }
}
