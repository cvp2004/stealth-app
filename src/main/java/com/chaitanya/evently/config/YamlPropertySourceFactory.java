package com.chaitanya.evently.config;

import java.io.IOException;
import java.util.Objects;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class YamlPropertySourceFactory implements PropertySourceFactory {
    @Override
    public @NonNull PropertySource<?> createPropertySource(@Nullable String name, @NonNull EncodedResource resource)
            throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        String sourceName = name != null ? name : Objects.requireNonNull(resource.getResource().getFilename());
        return new PropertiesPropertySource(sourceName, Objects.requireNonNull(factory.getObject()));
    }
}
