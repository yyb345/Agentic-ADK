/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.utils;

import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 19:07
 */
public class AssertUtils {

    public static <T> T assertNotIn(T object, Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BaseException("collection is empty.", ErrorEnum.SYSTEM_ERROR);
        }
        if (collection.contains(object)) {
            throw new BaseException("object in collection.", ErrorEnum.SYSTEM_ERROR);
        }
        return object;
    }

    public static <T> T assertIn(T object, Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BaseException("collection is empty.", ErrorEnum.SYSTEM_ERROR);
        }
        if (object != null && collection.contains(object)) {
            return object;
        }
        throw new BaseException("object not in collection.", ErrorEnum.SYSTEM_ERROR);
    }

    public static <T> T assertNotNull(T object) {
        if (object == null) {
            throw new BaseException("object is null.", ErrorEnum.SYSTEM_ERROR);
        }
        return object;
    }

    public static Boolean assertTrue(Boolean bool) {
        if (Boolean.TRUE.equals(bool)) {
            return bool;
        }
        throw new BaseException("not true.", ErrorEnum.SYSTEM_ERROR);
    }

    public static String assertNotBlank(String str) {
        if (StringUtils.isBlank(str)) {
            throw new BaseException("str is null.", ErrorEnum.SYSTEM_ERROR);
        }
        return str;
    }

    public static String assertNotBlank(String str, String errorMsg) {
        if (StringUtils.isBlank(str)) {
            throw new BaseException(errorMsg, ErrorEnum.SYSTEM_ERROR);
        }
        return str;
    }

}
