package com.alibaba.langengine.mcp.spec;

public enum LoggingLevel {// @formatter:off
    DEBUG(0),
    INFO(1),
    NOTICE(2),
    WARNING(3),
    ERROR(4),
    CRITICAL(5),
    ALERT(6),
    EMERGENCY(7);

    private final int level;

    LoggingLevel(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

}