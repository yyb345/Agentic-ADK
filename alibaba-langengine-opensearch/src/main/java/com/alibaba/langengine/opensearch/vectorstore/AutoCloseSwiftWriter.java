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
package com.alibaba.langengine.opensearch.vectorstore;

import com.alibaba.search.swift.SwiftClient;
import com.alibaba.search.swift.exception.SwiftException;
import com.alibaba.search.swift.protocol.ErrCode;
import com.alibaba.search.swift.protocol.SwiftMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AutoCloseSwiftWriter implements AutoCloseable{

    private final SwiftClient swiftClient;

    @Getter
    private final com.alibaba.search.swift.SwiftWriter swiftWriter;

    public AutoCloseSwiftWriter(String clientConfig, String writerConfig) throws SwiftException {
        this.swiftClient = new SwiftClient();
        swiftClient.init(clientConfig);
        this.swiftWriter = swiftClient.createWriter(writerConfig);
    }

    /**
     * <a href=https://yuque.antfin-inc.com/iregap/gervlu/zdm5eq>从0接入Swift</a>
     * <a href=https://yuque.antfin-inc.com/iregap/ivd2ee/xkklba>SwiftWriter参数</a>
     * @param zkPath topic所有的swift地址，用于client 同步连接多个swift
     * @param topicName 要读/写的swift topic
     * @param messageInfo {@link }
     * @throws Exception Exception
     */
    public static void write(String zkPath, String topicName, SwiftMessage.WriteMessageInfo messageInfo) throws Exception {
        String configStr = "zkPath="+ zkPath;
        String functionChain = "HASH,hashId2partId";
        String writerConfig = "topicName=" + topicName + ";" + "functionChain=" + functionChain;

        try(AutoCloseSwiftWriter swiftWriter =  new AutoCloseSwiftWriter(configStr,writerConfig)) {
            swiftWriter.writeMsg(messageInfo);
        }
    }

    /**
     * 同步等待buffer刷到broker，并落盘，通常时间比较久，与swift broker刷盘时间相关，一般为2-10秒
     * @warning  为了提高写吞吐，可以不用waitFinished
     */
    public void waitFinished() throws SwiftException {
        swiftWriter.waitFinished(20 * 1000 * 1000);
    }

    @Override
    public void close() throws Exception {
        if (swiftWriter != null) {
            // close时释放内存，否则内存泄露
            // 该函数可被多次调用，后面的调用无操作，但不能保证并发调用的线程安全
            swiftWriter.close();
        }
        // 最后释放swiftClient,client内存释放前必须先close由其创建的writer和reader
        swiftClient.close();
    }

    /**
     * 发送消息，如果不是因为Buffer溢出发送失败会抛异常，如果是会间隔50m后不断重试最多10次
     * @param messageInfo messageInfo
     * @throws SwiftException SwiftException
     */
    private void writeMsg(SwiftMessage.WriteMessageInfo messageInfo) throws SwiftException {
        int retryTimes = 0;
        final int maxRetryTimes = 10;
        writeMsgWithLimit( messageInfo,retryTimes, maxRetryTimes);
    }

    private void writeMsgWithLimit(SwiftMessage.WriteMessageInfo messageInfo, int retryTimes, int maxRetryTimes) throws SwiftException {
        try {
            this.swiftWriter.write(messageInfo);
        } catch (SwiftException e) {
            if (e.getEc() == ErrCode.ErrorCode.ERROR_CLIENT_SEND_BUFFER_FULL) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    log.error("writeMsg | ",ex);
                }
                writeMsgWithLimit(messageInfo,retryTimes,maxRetryTimes);
            } else {
                throw e;
            }
        }
        retryTimes++;
        if(retryTimes > maxRetryTimes){
            throw new RuntimeException("已达到最大重试次数:"+ maxRetryTimes);
        }

    }
}