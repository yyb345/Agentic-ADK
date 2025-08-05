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
package com.alibaba.langengine.xinghuo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import okhttp3.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;

/**
 * @author liuchunhe.lch on 2023/9/10 18:55
 * well-meaning people get together do meaningful things
 **/
@Data
public class WebSocketListener extends okhttp3.WebSocketListener {
    public static final Gson json = new Gson();
    public String answer = "";
    public WebSocket webSocket = null;

    private Consumer<String> consumer;

    private Boolean wsCloseFlag = false;

    public WebSocketListener(Consumer<String> consumer) {
        setConsumer(consumer);
    }
    public void createWebSocket() throws InterruptedException {
        try {
            if (webSocket == null) {
                //构建鉴权httpurl
                String authUrl = getAuthorizationUrl(XinghuoConfiguration.XINGHUO_SERVER_URL, XinghuoConfiguration.XINGHUO_API_KEY, XinghuoConfiguration.XINGHUO_API_SECRET);
                OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                String url = authUrl.replace("https://", "wss://").replace("http://", "ws://");
                Request request = new Request.Builder().url(url).build();
                webSocket = okHttpClient.newWebSocket(request, this);
            }
        } catch (Exception e) {
        }
    }
    /**
     * 对url进行鉴权
     *
     * @param hostUrl
     * @param apikey
     * @param apisecret
     * @return
     * @throws Exception
     */
    public static String getAuthorizationUrl(String hostUrl, String apikey, String apisecret) throws Exception {
        //获取host
        URL url = new URL(hostUrl);
        //获取鉴权时间 date
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        //获取signature_origin字段
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").
                append("date: ").append(date).append("\n").
                append("GET ").append(url.getPath()).append(" HTTP/1.1");
        //获得signature
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec sp = new SecretKeySpec(apisecret.getBytes(charset), "hmacsha256");
        mac.init(sp);
        byte[] basebefore = mac.doFinal(builder.toString().getBytes(charset));
        String signature = Base64.getEncoder().encodeToString(basebefore);
        //获得 authorization_origin
        String authorization_origin = String.format("api_key=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"", apikey, "hmac-sha256", "host date request-line", signature);
        //获得authorization
        String authorization = Base64.getEncoder().encodeToString(authorization_origin.getBytes(charset));
        //获取httpurl
        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().//
                addQueryParameter("authorization", authorization).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();
        return httpUrl.toString();
    }
    public void sendMsg(WebSocket webSocket, String uid, int maxTokens,double temperature, String question) {
        JsonObject frame = new JsonObject();
        JsonObject header = new JsonObject();
        JsonObject chat = new JsonObject();
        JsonObject parameter = new JsonObject();
        JsonObject payload = new JsonObject();
        JsonObject message = new JsonObject();
        JsonObject text = new JsonObject();
        JsonArray ja = new JsonArray();
        //填充header
        header.addProperty("app_id", XinghuoConfiguration.XINGHUO_APP_ID);
        header.addProperty("uid", uid);
        //填充parameter
        chat.addProperty("domain", "generalv2");
        chat.addProperty("random_threshold", 0);
        chat.addProperty("max_tokens", maxTokens);
        chat.addProperty("temperature", temperature);
        chat.addProperty("auditing", "default");
        parameter.add("chat", chat);
        //填充payload
        text.addProperty("role", "user");
        text.addProperty("content", question);
        ja.add(text);
        message.add("text", ja);
        payload.add("message", message);
        frame.add("header", header);
        frame.add("parameter", parameter);
        frame.add("payload", payload);
        webSocket.send(frame.toString());
    }
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        ResponseData responseData = json.fromJson(text, ResponseData.class);
        if (0 == responseData.getHeader().get("code").getAsInt()) {
            if (2 != responseData.getHeader().get("status").getAsInt()) {
                Payload pl = json.fromJson(responseData.getPayload(), Payload.class);
                JsonArray temp = (JsonArray) pl.getChoices().get("text");
                JsonObject jo = (JsonObject) temp.get(0);
                answer += jo.get("content").getAsString();
            } else {
                Payload pl1 = json.fromJson(responseData.getPayload(), Payload.class);
                JsonObject jsonObject = (JsonObject) pl1.getUsage().get("text");
                JsonArray temp1 = (JsonArray) pl1.getChoices().get("text");
                JsonObject jo = (JsonObject) temp1.get(0);
                answer += jo.get("content").getAsString();
                wsCloseFlag = true;
            }
        } else {
            System.out.println("返回结果错误：\n" + responseData.getHeader().get("code") + responseData.getHeader().get("message"));
        }
    }
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
    }
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        System.out.println(response);
    }
    class ResponseData {
        private JsonObject header;
        private JsonObject payload;
        public JsonObject getHeader() {
            return header;
        }
        public JsonObject getPayload() {
            return payload;
        }
    }
    class Header {
        private int code;
        private String message;
        private String sid;
        private String status;
        public int getCode() {
            return code;
        }
        public String getMessage() {
            return message;
        }
        public String getSid() {
            return sid;
        }
        public String getStatus() {
            return status;
        }
    }
    class Payload {
        private JsonObject choices;
        private JsonObject usage;
        public JsonObject getChoices() {
            return choices;
        }
        public JsonObject getUsage() {
            return usage;
        }
    }
    class Choices {
        private int status;
        private int seq;
        private JsonArray text;
        public int getStatus() {
            return status;
        }
        public int getSeq() {
            return seq;
        }
        public JsonArray getText() {
            return text;
        }
    }
}
