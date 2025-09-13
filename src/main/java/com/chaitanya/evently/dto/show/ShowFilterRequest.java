package com.chaitanya.evently.dto.show;

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
public class ShowFilterRequest {

    private Long venueId;
    private Long eventId;
    private Instant date;
    private Instant fromDate;
    private Instant toDate;
}
