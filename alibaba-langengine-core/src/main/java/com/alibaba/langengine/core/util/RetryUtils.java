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
package com.alibaba.langengine.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.HttpException;

import java.util.concurrent.Callable;

import static java.lang.String.format;

public class RetryUtils {

    private static final int HTTP_CODE_401_UNAUTHORIZED = 401;
    private static final int HTTP_CODE_429_TOO_MANY_REQUESTS = 429;

    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);

    /**
     * This method attempts to execute a given action up to a specified number of times.
     * If the action fails on all attempts, it throws a RuntimeException.
     * Retry will not happen for 401 (Unauthorized).
     * Retry will happen after 1-second delay for 429 (Too many requests).
     *
     * @param action      The action to be executed.
     * @param maxAttempts The maximum number of attempts to execute the action.
     * @return The result of the action if it is successful.
     * @throws RuntimeException if the action fails on all attempts.
     */
    public static <T> T withRetry(Callable<T> action, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // 10毫秒重试一次
                Thread.sleep(10);
                return action.call();
            } catch (HttpException e) {
                if (attempt == maxAttempts) {
                    throw new RuntimeException(e);
                }

                if (e.code() == HTTP_CODE_401_UNAUTHORIZED) {
                    throw new RuntimeException(e); // makes no sense to retry
                }

                log.warn(format("Exception was thrown on attempt %s of %s", attempt, maxAttempts), e);

                if (e.code() == HTTP_CODE_429_TOO_MANY_REQUESTS) {
                    try {
                        // TODO make configurable or read from Retry-After
                        Thread.sleep(1000); // makes sense to retry after a bit of waiting
                    } catch (InterruptedException ignored) {
                    }
                }
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw new RuntimeException(e);
                }
                log.warn(format("Exception was thrown on attempt %s of %s", attempt, maxAttempts), e);
            }
        }
        throw new RuntimeException("Failed after " + maxAttempts + " attempts");
    }
}
