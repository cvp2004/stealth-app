package com.chaitanya.evently.util;

import com.chaitanya.evently.exception.types.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;

public class HeaderUtil {

    public static Long getUserIdFromHeader(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-ID");

        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new UnauthorizedException("X-User-ID header is required");
        }

        try {
            return Long.parseLong(userIdHeader);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid User-ID");
        }
    }

    public static void validateAdminHeader(HttpServletRequest request) {
        String adminHeader = request.getHeader("X-Admin-User");

        if (adminHeader == null || !("true".equalsIgnoreCase(adminHeader.trim()))) {
            throw new UnauthorizedException("X-Admin-User header must be set to 'true' for admin access");
        }
    }
}