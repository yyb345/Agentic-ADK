/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Result of a sampling/createMessage request.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CreateMessageResult {
    private final Role role;
    private final Content content;
    private final String model;
    private final StopReason stopReason;

    public CreateMessageResult(
            @JsonProperty("role") Role role,
            @JsonProperty("content") Content content,
            @JsonProperty("model") String model,
            @JsonProperty("stopReason") StopReason stopReason) {
        this.role = role;
        this.content = content;
        this.model = model;
        this.stopReason = stopReason;
    }

    public Role role() {
        return role;
    }

    public Content content() {
        return content;
    }

    public String model() {
        return model;
    }

    public StopReason stopReason() {
        return stopReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateMessageResult that = (CreateMessageResult) o;
        return role == that.role &&
               Objects.equals(content, that.content) &&
               Objects.equals(model, that.model) &&
               stopReason == that.stopReason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content, model, stopReason);
    }

    @Override
    public String toString() {
        return "CreateMessageResult{" +
               "role=" + role +
               ", content=" + content +
               ", model='" + model + '\'' +
               ", stopReason=" + stopReason +
               '}';
    }

    /**
     * Reason for stopping the generation.
     */
    public enum StopReason {
        @JsonProperty("endTurn") END_TURN,
        @JsonProperty("stopSequence") STOP_SEQUENCE,
        @JsonProperty("maxTokens") MAX_TOKENS
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Role role = Role.ASSISTANT;
        private Content content;
        private String model;
        private StopReason stopReason = StopReason.END_TURN;

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder content(Content content) {
            this.content = content;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder stopReason(StopReason stopReason) {
            this.stopReason = stopReason;
            return this;
        }

        public Builder message(String message) {
            this.content = new TextContent(message);
            return this;
        }

        public CreateMessageResult build() {
            return new CreateMessageResult(role, content, model, stopReason);
        }
    }
}
