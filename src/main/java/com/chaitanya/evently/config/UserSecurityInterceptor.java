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
public class UserSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // Check if the request is to a user endpoint (but not auth endpoints)
        if (requestURI.startsWith("/api/v1/user/") && !requestURI.startsWith("/api/v1/auth/")) {
            log.info("Validating user access for request: {} {}", request.getMethod(), requestURI);

            try {
                Long userId = HeaderUtil.getUserIdFromHeader(request);
                log.info("User access validated successfully for user ID: {} on request: {}", userId, requestURI);
                return true;
            } catch (Exception e) {
                log.warn("User access denied for request: {} - {}", requestURI, e.getMessage());
                throw e; // Re-throw the exception to be handled by the global exception handler
            }
        }

        // Allow non-user requests and auth requests to proceed
        return true;
    }
}
