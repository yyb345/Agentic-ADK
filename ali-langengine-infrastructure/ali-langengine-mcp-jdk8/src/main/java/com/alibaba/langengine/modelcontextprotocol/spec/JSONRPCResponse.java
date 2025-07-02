package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author: aihe.ah
 * @date: 2025/4/2
 * 功能描述：
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class JSONRPCResponse implements JSONRPCMessage {
    private final String jsonrpc;
    private final Object id;
    private final Object result;
    private final JSONRPCError error;

    public JSONRPCResponse(
            @JsonProperty("jsonrpc") String jsonrpc,
            @JsonProperty("id") Object id,
            @JsonProperty("result") Object result,
            @JsonProperty("error") JSONRPCError error) {
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.result = result;
        this.error = error;
    }

    @Override
    public String jsonrpc() {
        return jsonrpc;
    }

    public Object id() {
        return id;
    }

    public Object result() {
        return result;
    }

    public JSONRPCError error() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSONRPCResponse that = (JSONRPCResponse) o;
        return Objects.equals(jsonrpc, that.jsonrpc) &&
                Objects.equals(id, that.id) &&
                Objects.equals(result, that.result) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonrpc, id, result, error);
    }

    @Override
    public String toString() {
        return "JSONRPCResponse{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", id=" + id +
                ", result=" + result +
                ", error=" + error +
                '}';
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
@Data
    public static class JSONRPCError {
        private final int code;
        private final String message;
        private final Object data;

        public JSONRPCError(
                @JsonProperty("code") int code,
                @JsonProperty("message") String message,
                @JsonProperty("data") Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public int code() {
            return code;
        }

        public String message() {
            return message;
        }

        public Object data() {
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JSONRPCError that = (JSONRPCError) o;
            return code == that.code &&
                    Objects.equals(message, that.message) &&
                    Objects.equals(data, that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code, message, data);
        }

        @Override
        public String toString() {
            return "JSONRPCError{" +
                    "code=" + code +
                    ", message='" + message + '\'' +
                    ", data=" + data +
                    '}';
        }
    }
}
