package com.chaitanya.evently.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        private String property;
        private String direction;
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
}
