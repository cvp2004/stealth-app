package com.chaitanya.evently.util;

import com.chaitanya.evently.config.SortConfigProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SortParameterValidator {

    private final SortConfigProperties sortConfigProperties;

    public Sort resolve(String endpointKey, String sortParam, String defaultProperty) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.ASC, defaultProperty);
        }

        List<String> allowed = getAllowedFor(endpointKey);
        List<Sort.Order> orders = new ArrayList<>();

        // Support multiple sort fields separated by semicolon
        String[] sortFields = sortParam.split(";");

        for (String sortField : sortFields) {
            String[] parts = sortField.trim().split(",");
            String property = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            if (!allowed.contains(property)) {
                throw new IllegalArgumentException(
                        "Invalid sort property: " + property + ". Allowed: " + String.join(", ", allowed));
            }

            orders.add(new Sort.Order(direction, property));
        }

        return Sort.by(orders);
    }

    public List<String> parseSortFields(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return List.of();
        }

        return Arrays.stream(sortParam.split(";"))
                .map(field -> field.trim().split(",")[0].trim())
                .collect(Collectors.toList());
    }

    private List<String> getAllowedFor(String endpointKey) {
        Map<String, SortConfigProperties.EndpointSort> endpoints = sortConfigProperties.getEndpoints();
        SortConfigProperties.EndpointSort endpoint = endpoints.get(endpointKey);
        return endpoint != null ? endpoint.getAllowed() : List.of();
    }
}