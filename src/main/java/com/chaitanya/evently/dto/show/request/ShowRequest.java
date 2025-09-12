package com.chaitanya.evently.dto.show.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
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
public class ShowRequest {

    @NotNull(message = "venueId is required")
    private Long venueId;

    @NotNull(message = "eventId is required")
    private Long eventId;

    @NotNull(message = "startTimestamp is required")
    @Future(message = "startTimestamp must be in the future")
    private java.time.Instant startTimestamp;

    @NotNull(message = "durationMinutes is required")
    @jakarta.validation.constraints.Min(value = 1, message = "durationMinutes must be at least 1")
    private Integer durationMinutes;
}
