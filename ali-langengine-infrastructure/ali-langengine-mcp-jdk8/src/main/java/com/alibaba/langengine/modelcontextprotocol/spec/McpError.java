/*
* Copyright 2024 - 2024 the original author or authors.
*/
package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;


public class McpError extends RuntimeException {

	private JSONRPCResponse.JSONRPCError jsonRpcError;

	public McpError(JSONRPCResponse.JSONRPCError jsonRpcError) {
		super(jsonRpcError.message());
		this.jsonRpcError = jsonRpcError;
	}

	public McpError(Object error) {
		super(error.toString());
	}

	public JSONRPCResponse.JSONRPCError getJsonRpcError() {
		return jsonRpcError;
	}

}