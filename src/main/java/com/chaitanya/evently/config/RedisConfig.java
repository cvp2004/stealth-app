
package com.chaitanya.evently.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url:redis://localhost:6379/0}")
    private String redisUrl;

    @Value("${spring.data.redis.timeout:2000}")
    private int redisTimeout;

    @Bean
    public JedisPool jedisPool() {
        try {
            URI uri = new URI(redisUrl);

            String host = uri.getHost();
            int port = uri.getPort();
            String userInfo = uri.getUserInfo(); // might be "user:password" or just "password"
            String password = null;
            if (userInfo != null && userInfo.contains(":")) {
                password = userInfo.split(":", 2)[1];
            } else if (userInfo != null) {
                password = userInfo;
            }

            int database = 0;
            if (uri.getPath() != null && !uri.getPath().isEmpty()) {
                database = Integer.parseInt(uri.getPath().substring(1));
            }

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(20);
            poolConfig.setMaxIdle(10);
            poolConfig.setMinIdle(5);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);

            return new JedisPool(poolConfig, host, port, redisTimeout, password, database);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Redis URL: " + redisUrl, e);
        }
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();
        return mapper;
    }
}