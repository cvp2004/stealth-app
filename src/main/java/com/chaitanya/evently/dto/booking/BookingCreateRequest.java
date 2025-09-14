package com.chaitanya.evently.dto.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
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
public class BookingCreateRequest {

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotEmpty(message = "Seats list cannot be empty")
    private List<SeatRequest> seats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatRequest {
        @NotNull(message = "Section is required")
        private String section;

        @NotNull(message = "Row is required")
        private String row;

        @NotNull(message = "Seat number is required")
        private String seatNumber;
    }
}
