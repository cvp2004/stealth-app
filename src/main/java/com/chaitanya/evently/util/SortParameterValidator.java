package com.chaitanya.evently.util;

import com.chaitanya.evently.config.SortConfigProperties;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SortParameterValidator {

    private final SortConfigProperties sortConfigProperties;

    public Sort resolve(String endpointKey, String sortParam, String defaultProperty) {
        String property;
        Sort.Direction direction;

        if (sortParam == null || sortParam.isBlank()) {
            property = defaultProperty;
            direction = Sort.Direction.ASC;
        } else {
            String[] parts = sortParam.split(",");
            property = parts[0];
            direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
        }

        List<String> allowed = getAllowedFor(endpointKey);
        if (!allowed.contains(property)) {
            throw new IllegalArgumentException(
                    "Invalid sort property: " + property + ". Allowed: " + String.join(", ", allowed));
        }

        return Sort.by(direction, property);
    }

    private List<String> getAllowedFor(String endpointKey) {
        Map<String, SortConfigProperties.EndpointSort> endpoints = sortConfigProperties.getEndpoints();
        SortConfigProperties.EndpointSort endpoint = endpoints.get(endpointKey);
        return endpoint != null ? endpoint.getAllowed() : List.of();
    }
}