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
package com.alibaba.agentic.core.executor;

import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/4 17:52
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Result {

    Map<String, Object> data;
    private boolean success;
    private String code;
    private String errorMsg;

    public Result(boolean success, String code, String errorMsg, Map<String, Object> data) {
        this.success = success;
        this.code = code;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    public static Result success(Map<String, Object> data) {
        return new Result(true, "200", null, data);
    }


    public static Result fail(Throwable throwable) {
        if (throwable instanceof BaseException exception) {
            return new Result(false, exception.getErrorEnum().getCode(), exception.getMessage(), null);
        } else {
            return new Result(false, ErrorEnum.SYSTEM_ERROR.getCode(), ExceptionUtils.getStackTrace(throwable), null);
        }
    }

}
