package com.alibaba.agentic.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 20:27
 */
@SpringBootApplication(scanBasePackages = {"com.alibaba.agentic.core"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
