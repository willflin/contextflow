package com.contextflow.ai.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "contextflow.agent")
public class AgentProperties {

    private Provider provider = Provider.LOCAL;
    private boolean fallbackToLocalOnError = true;

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public boolean isFallbackToLocalOnError() {
        return fallbackToLocalOnError;
    }

    public void setFallbackToLocalOnError(boolean fallbackToLocalOnError) {
        this.fallbackToLocalOnError = fallbackToLocalOnError;
    }

    public enum Provider {
        LOCAL,
        SPRING_AI
    }
}
