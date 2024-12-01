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
package com.alibaba.agentmagic.framework.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * Snowflake分布式唯一ID算法生成器
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class SnowflakeIdGenerator {

    // 初始纪元时间戳 (2023-01-01)
    private final long epoch = 1672531200000L;

    // 各部分的位数
    private final long workerIdBits = 10L;
    private final long sequenceBits = 12L;

    // 最大值计算
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxSequence = -1L ^ (-1L << sequenceBits);

    // 位移操作需要用到的值
    private final long workerIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits;

    // 工作者ID和序列号
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    // 初始化生成器
    public SnowflakeIdGenerator() {
        workerId = getWorkerIdFromMAC();
        log.info("getWorkerIdFromMAC:" + workerId);
//        if (workerId > maxWorkerId || workerId < 0) {
//            throw new IllegalArgumentException(String.format("workerId can't be greater than %d or less than 0", maxWorkerId));
//        }
    }

    // 生成下一个ID
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - epoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private static long getWorkerIdFromMAC() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    return ((long) mac[mac.length - 2] & 0xFF) << 8 | ((long) mac[mac.length - 1] & 0xFF);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void main(String[] args) {
        SnowflakeIdGenerator idWorker = new SnowflakeIdGenerator();
        for (int i = 0; i < 10; i++) {
            long id = idWorker.nextId();
            System.out.println(id);
        }
    }
}
