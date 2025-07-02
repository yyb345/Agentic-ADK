package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * @author: aihe.ah
 * @date: 2025/4/2
 * 功能描述：
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class JSONRPCNotification implements JSONRPCMessage {
    private final String jsonrpc;
    private final String method;
    private final Map<String, Object> params;

    public JSONRPCNotification(
            @JsonProperty("jsonrpc") String jsonrpc,
            @JsonProperty("method") String method,
            @JsonProperty("params") Map<String, Object> params) {
        this.jsonrpc = jsonrpc;
        this.method = method;
        this.params = params;
    }

    @Override
    public String jsonrpc() {
        return jsonrpc;
    }

    public String method() {
        return method;
    }

    public Map<String, Object> params() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSONRPCNotification that = (JSONRPCNotification) o;
        return Objects.equals(jsonrpc, that.jsonrpc) &&
                Objects.equals(method, that.method) &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonrpc, method, params);
    }

    @Override
    public String toString() {
        return "JSONRPCNotification{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", method='" + method + '\'' +
                ", params=" + params +
                '}';
    }
}
