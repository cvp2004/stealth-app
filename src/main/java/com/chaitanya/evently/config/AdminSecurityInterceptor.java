package com.chaitanya.evently.config;

import com.chaitanya.evently.util.HeaderUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AdminSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // Check if the request is to an admin endpoint
        if (requestURI.startsWith("/api/v1/admin/")) {
            log.info("Validating admin access for request: {} {}", request.getMethod(), requestURI);

            try {
                HeaderUtil.validateAdminHeader(request);
                log.info("Admin access validated successfully for request: {}", requestURI);
                return true;
            } catch (Exception e) {
                log.warn("Admin access denied for request: {} - {}", requestURI, e.getMessage());
                throw e; // Re-throw the exception to be handled by the global exception handler
            }
        }

        // Allow non-admin requests to proceed
        return true;
    }
}
