package com.chaitanya.evently.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:sort.config.yml", factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties(SortConfigProperties.class)
public class AdditionalYamlConfig {
}
