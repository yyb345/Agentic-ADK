///**
// * Copyright (C) 2024 AIDC-AI
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.langengine.mcp.shared;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.alibaba.langengine.mcp.spec.JSONRPCMessage;
//import lombok.extern.slf4j.Slf4j;
//
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//
//@Slf4j
//public class ReadBuffer {
//    private ByteBuffer buffer = ByteBuffer.allocate(1024);
//
//    public void append(byte[] chunk) {
//        if (buffer.remaining() < chunk.length) {
//            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
//            buffer.flip();
//            newBuffer.put(buffer);
//            buffer = newBuffer;
//        }
//        buffer.put(chunk);
//    }
//
//    public JSONRPCMessage readMessage() {
//        buffer.flip();
//        int limit = buffer.limit();
//        for (int i = 0; i < limit; i++) {
//            if (buffer.get(i) == '\n') {
//                byte[] lineBytes = new byte[i];
//                buffer.get(lineBytes, 0, i);
//                buffer.position(i + 1);
//                buffer.compact();
//                String line = new String(lineBytes, StandardCharsets.UTF_8).trim();
//                return deserializeMessage(line);
//            }
//        }
//        buffer.compact();
//        return null;
//    }
//
//    public void clear() {
//        buffer.clear();
//    }
//
//    private JSONRPCMessage deserializeMessage(String line) {
//        try {
//            log.info("line:{}", line);
//            return JSON.parseObject(line, JSONRPCMessage.class);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to deserialize JSON-RPC message", e);
//        }
//    }
//
//    public String serializeMessage(JSONRPCMessage message) {
//        try {
//            return JSON.toJSONString(message, SerializerFeature.WriteClassName) + "\n";
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to serialize JSON-RPC message", e);
//        }
//    }
//}
