package com.chaitanya.evently.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AdminSecurityInterceptor adminSecurityInterceptor;
    private final UserSecurityInterceptor userSecurityInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Admin interceptor - requires X-Admin-User: true header
        registry.addInterceptor(adminSecurityInterceptor)
                .addPathPatterns("/api/v1/admin/**");

        // User interceptor - requires X-User-ID header (excludes auth endpoints)
        registry.addInterceptor(userSecurityInterceptor)
                .addPathPatterns("/api/v1/user/**")
                .excludePathPatterns("/api/v1/auth/**");
    }
}
