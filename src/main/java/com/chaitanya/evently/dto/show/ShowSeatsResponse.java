package com.chaitanya.evently.dto.show;

import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeatsResponse {

    private SeatMapResponse seatMap;
    private List<Long> bookedSeatIds;
    private Long showId;
    private String showName;
    private String eventName;
    private String venueName;
}
