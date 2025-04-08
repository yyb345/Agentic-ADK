/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.alibaba.langengine.mcp.server;

import com.alibaba.langengine.mcp.server.transport.HttpServletSseServerTransport;
import com.alibaba.langengine.mcp.spec.ServerMcpTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Timeout;

/***
 * @author Christian Tzolov
 */
@Timeout(15) // Giving extra time beyond the client timeout
class ServletSseMcpSyncServerTests extends AbstractMcpSyncServerTests {

	@Override
	protected ServerMcpTransport createMcpTransport() {
		return new HttpServletSseServerTransport("/mcp/message");
	}

}
