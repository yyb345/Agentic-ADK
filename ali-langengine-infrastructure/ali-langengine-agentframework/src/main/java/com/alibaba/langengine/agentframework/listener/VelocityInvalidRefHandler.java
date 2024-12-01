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
package com.alibaba.langengine.agentframework.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.Info;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * description
 *
 * @Author zhishan
 * @Date 2024-10-15
 */
@Slf4j
public class VelocityInvalidRefHandler implements InvalidReferenceEventHandler {

    private static final Map<String, Function<String, Object>> invalidGetMethodFunctionMap = new HashMap<>();

    public static void registerInvalidGetMethodFunction(String key, Function<String, Object> function) {
        invalidGetMethodFunctionMap.put(key, function);
    }

    /**
     * velocity替换的时候，如果占位符匹配不到真是变量会调用这个方法
     * @param context the context when the reference was found invalid
     * @param reference string with complete invalid reference. If silent reference, will start with $!
     * @param object the object referred to, or null if not found
     * @param property the property name from the reference
     * @param info contains template, line, column details
     * @return
     */
    @Override
    public Object invalidGetMethod(Context context, String reference, Object object, String property, Info info) {
        // 运行细节可运行 DelegationHelperTest.replaceBusinessJsonTest 观察
        if (info == null || !info.getTemplateName().startsWith("com.")) {
            return null;
        }
        Function<String, Object> function = invalidGetMethodFunctionMap.get(info.getTemplateName());
        if (function != null) {
            return function.apply(reference);
        }
        return null;
    }

    @Override
    public boolean invalidSetMethod(Context context, String leftreference, String rightreference, Info info) {
        log.warn("invalidSetMethod for leftreference:{}, rightreference:{}", leftreference, rightreference);
        return false;
    }

    @Override
    public Object invalidMethod(Context context, String reference, Object object, String method, Info info) {
        log.warn("invalidSetMethod for reference:{}", reference);
        return reference;
    }
}
