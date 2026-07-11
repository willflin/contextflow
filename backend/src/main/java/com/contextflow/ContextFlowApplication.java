package com.contextflow;

import com.contextflow.ai.agent.config.AgentProperties;
import com.contextflow.auth.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, AgentProperties.class})
@EnableScheduling
public class ContextFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContextFlowApplication.class, args);
    }
}
