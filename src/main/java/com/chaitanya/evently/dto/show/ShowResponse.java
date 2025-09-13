package com.chaitanya.evently.dto.show;

import com.chaitanya.evently.model.status.ShowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowResponse {

    private Long id;
    private Long venueId;
    private String venueName;
    private Long eventId;
    private String eventTitle;
    private String eventDescription;
    private String eventCategory;
    private Instant startTimestamp;
    private Integer durationMinutes;
    private ShowStatus status;
    private Instant createdAt;
}
