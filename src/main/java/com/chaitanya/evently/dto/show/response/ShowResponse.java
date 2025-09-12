package com.chaitanya.evently.dto.show.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowResponse {
    private Long id;
    private String refId;
    private String title;
    private String description;
    private Instant startTimestamp;
    private LocalDate date;
    private LocalTime startTime;
    private Integer durationMinutes;
    private VenueInfo venue;
    private EventInfo event; // Optional
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    public static class VenueInfo {
        private Long id;
        private String name;
        private String address;
    }

    @Data
    @Builder
    public static class EventInfo {
        private Long id;
        private String title;
        private String category;
    }
}
