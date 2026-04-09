package com.ipl.analysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class RedisConfig {

    @Value("${REDIS_URL:}")
    private String redisUrl;

    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() throws URISyntaxException {
        // Render sometimes injects an empty string instead of null
        if (redisUrl != null && !redisUrl.trim().isEmpty()) {
            URI uri = new URI(redisUrl);
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(uri.getHost());
            if (uri.getPort() != -1) {
                config.setPort(uri.getPort());
            }
            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":", 2);
                if (userInfo.length == 2) {
                    config.setPassword(userInfo[1]);
                }
            }
            return new LettuceConnectionFactory(config);
        }

        // Safe fallback
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }
}
