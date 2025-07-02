package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

/**
 * Standard error codes used in MCP JSON-RPC responses.
 */
public final class ErrorCodes {

    /**
     * Invalid JSON was received by the server.
     */
    public static final int PARSE_ERROR = -32700;

    /**
     * The JSON sent is not a valid Request object.
     */
    public static final int INVALID_REQUEST = -32600;

    /**
     * The method does not exist / is not available.
     */
    public static final int METHOD_NOT_FOUND = -32601;

    /**
     * Invalid method parameter(s).
     */
    public static final int INVALID_PARAMS = -32602;

    /**
     * Internal JSON-RPC error.
     */
    public static final int INTERNAL_ERROR = -32603;
}
