#!/bin/bash

# 运行WeatherClient示例，启用详细日志
cd "$(dirname "$0")"

# 确保Logback依赖被正确加载
mvn clean compile exec:java \
  -Dexec.mainClass="com.alibaba.langengine.modelcontextprotocol.examples.WeatherClient" \
  -Dlogback.debug=true \
  -Dexec.cleanupDaemonThreads=false \
  -Dexec.classpathScope=compile \
  -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
