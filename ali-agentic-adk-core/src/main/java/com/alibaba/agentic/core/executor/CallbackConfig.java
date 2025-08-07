package com.alibaba.agentic.core.executor;

import org.springframework.lang.NonNull;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 11:34
 */
public interface CallbackConfig {

    @NonNull
    default TYPE getType() {
        return TYPE.before;
    }

    enum TYPE {
        before,
        after
    }

}
