package com.chaitanya.evently.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationResponse<T> {
    private boolean isPaginated;
    private List<T> content;
    private PageMeta page;
    private SortMeta sort;
    private Links links;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageMeta {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SortMeta {
        private List<SortField> fields;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class SortField {
            private String property;
            private String direction;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Links {
        private String self;
        private String first;
        private String last;
        private String next;
        private String prev;
    }

    /**
     * Maps Spring Data Page to PaginationResponse
     */
    public static <T> PaginationResponse<T> fromPage(Page<T> page, String baseUrl) {
        List<SortMeta.SortField> sortFields = page.getSort().stream()
                .map(order -> SortMeta.SortField.builder()
                        .property(order.getProperty())
                        .direction(order.getDirection().name().toLowerCase())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return PaginationResponse.<T>builder()
                .isPaginated(page.getTotalPages() > 1)
                .content(page.getContent())
                .page(PageMeta.builder()
                        .number(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .sort(SortMeta.builder()
                        .fields(sortFields)
                        .build())
                .links(buildLinks(page, baseUrl))
                .build();
    }

    /**
     * Maps Spring Data Page to PaginationResponse with query parameters
     */
    public static <T> PaginationResponse<T> fromPage(Page<T> page, String baseUrl, String category) {
        PaginationResponse<T> response = fromPage(page, baseUrl);

        // Add category to links if present
        if (category != null && !category.trim().isEmpty()) {
            response.getLinks().setSelf(buildUrlWithParams(baseUrl, page.getNumber(), page.getSize(), category));
            response.getLinks().setFirst(buildUrlWithParams(baseUrl, 0, page.getSize(), category));
            response.getLinks()
                    .setLast(buildUrlWithParams(baseUrl, page.getTotalPages() - 1, page.getSize(), category));

            if (page.hasNext()) {
                response.getLinks()
                        .setNext(buildUrlWithParams(baseUrl, page.getNumber() + 1, page.getSize(), category));
            }
            if (page.hasPrevious()) {
                response.getLinks()
                        .setPrev(buildUrlWithParams(baseUrl, page.getNumber() - 1, page.getSize(), category));
            }
        }

        return response;
    }

    private static <T> Links buildLinks(Page<T> page, String baseUrl) {
        return Links.builder()
                .self(buildUrl(baseUrl, page.getNumber(), page.getSize()))
                .first(buildUrl(baseUrl, 0, page.getSize()))
                .last(buildUrl(baseUrl, page.getTotalPages() - 1, page.getSize()))
                .next(page.hasNext() ? buildUrl(baseUrl, page.getNumber() + 1, page.getSize()) : null)
                .prev(page.hasPrevious() ? buildUrl(baseUrl, page.getNumber() - 1, page.getSize()) : null)
                .build();
    }

    private static String buildUrl(String baseUrl, int page, int size) {
        return String.format("%s?page=%d&size=%d", baseUrl, page, size);
    }

    private static String buildUrlWithParams(String baseUrl, int page, int size, String category) {
        return String.format("%s?page=%d&size=%d&category=%s", baseUrl, page, size, category);
    }
}
