package com.alibaba.agentic.core.executor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/14 17:23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AsyncTaskResult extends Result {

    private String id;

    public AsyncTaskResult(boolean success, String code, String errorMsg, Map<String, Object> data, String id) {
        super(success, code, errorMsg, data);
        this.id = id;
    }

    public static AsyncTaskResult success(String id) {
        return new AsyncTaskResult(true, "200", null, null, id);
    }
}
