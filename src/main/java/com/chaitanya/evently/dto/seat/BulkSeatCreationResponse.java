package com.chaitanya.evently.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BulkSeatCreationResponse {
    private final int seatCount;
    private final int sectionCount;
    private final String venueName;
    private final int totalCapacity;
}
