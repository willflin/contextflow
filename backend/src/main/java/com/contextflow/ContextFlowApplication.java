package com.contextflow;

import com.contextflow.auth.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ContextFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContextFlowApplication.class, args);
    }
}
