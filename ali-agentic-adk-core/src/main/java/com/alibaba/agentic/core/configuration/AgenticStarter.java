package com.alibaba.agentic.core.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan(basePackages = {"com.alibaba.agentic", "com.google.adk"})
public class AgenticStarter {

}
