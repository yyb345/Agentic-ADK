package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author: aihe.ah
 * @date: 2025/4/2
 * 功能描述：
 */ // Existing Enums and Base Types (from previous implementation)
public enum Role {
    @JsonProperty("user") USER,
    @JsonProperty("assistant") ASSISTANT
}
