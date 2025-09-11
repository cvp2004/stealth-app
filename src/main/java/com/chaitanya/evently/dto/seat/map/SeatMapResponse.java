package com.chaitanya.evently.dto.seat.map;

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
public class SeatMapResponse {
    private String venueName;
    private int totalCapacity;
    private List<Section> sections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Section {
        private String sectionId;
        private List<Row> rows;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Row {
        private String rowId;
        private List<com.chaitanya.evently.dto.seat.SeatResponse> seats;
    }
}
