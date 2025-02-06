package com.alibaba.langengine.mcp.spec.schema;

import lombok.Getter;

@Getter
public class PaginatedResult {

    String nextCursor;

    public PaginatedResult(String nextCursor) {
        this.nextCursor = nextCursor;
    }
}
