package com.chaitanya.evently.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sort")
public class SortConfigProperties {
    private Map<String, EndpointSort> endpoints = Collections.emptyMap();

    @Getter
    @Setter
    public static class EndpointSort {
        private List<String> allowed = Collections.emptyList();
    }
}
