package com.chaitanya.evently.dto.seat.map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class SeatMapRequest {
    @NotNull(message = "sections are required")
    @NotEmpty(message = "sections cannot be empty")
    @Valid
    private List<Section> sections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Section {
        @NotNull(message = "sectionId is required")
        @NotBlank(message = "sectionId cannot be blank")
        @Size(min = 1, max = 50, message = "sectionId must be between 1 and 50 characters")
        private String sectionId;

        @NotNull(message = "rows are required")
        @NotEmpty(message = "rows cannot be empty")
        @Valid
        private List<Row> rows;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Row {
        @NotNull(message = "rowId is required")
        @NotBlank(message = "rowId cannot be blank")
        @Size(min = 1, max = 20, message = "rowId must be between 1 and 20 characters")
        private String rowId;

        @NotNull(message = "seatCount is required")
        @Min(value = 1, message = "seatCount must be at least 1")
        private Integer seatCount;
    }
}
