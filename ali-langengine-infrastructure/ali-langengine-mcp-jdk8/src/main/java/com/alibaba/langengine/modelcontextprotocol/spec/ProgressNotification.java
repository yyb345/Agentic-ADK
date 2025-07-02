/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Progress notification for long-running operations.
 * 
 * JDK 1.8 compatible version.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ProgressNotification {
    private final String progressToken;
    private final double progress;
    private final Double total;

    public ProgressNotification(
            @JsonProperty("progressToken") String progressToken,
            @JsonProperty("progress") double progress,
            @JsonProperty("total") Double total) {
        this.progressToken = progressToken;
        this.progress = progress;
        this.total = total;
    }

    public String progressToken() {
        return progressToken;
    }

    public double progress() {
        return progress;
    }

    public Double total() {
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressNotification that = (ProgressNotification) o;
        return Double.compare(that.progress, progress) == 0 &&
               Objects.equals(progressToken, that.progressToken) &&
               Objects.equals(total, that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(progressToken, progress, total);
    }

    @Override
    public String toString() {
        return "ProgressNotification{" +
               "progressToken='" + progressToken + '\'' +
               ", progress=" + progress +
               ", total=" + total +
               '}';
    }
}
