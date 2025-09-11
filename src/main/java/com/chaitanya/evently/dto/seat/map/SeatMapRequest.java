package com.chaitanya.evently.dto.seat.map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    @NotEmpty(message = "sections are required")
    @Valid
    private List<Section> sections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Section {
        @NotBlank(message = "sectionId is required")
        private String sectionId;

        @NotEmpty(message = "rows are required")
        @Valid
        private List<Row> rows;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Row {
        @NotBlank(message = "rowId is required")
        private String rowId;

        @Min(value = 1, message = "seatCount must be at least 1")
        private int seatCount;
    }
}
