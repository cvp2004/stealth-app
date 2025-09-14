package com.chaitanya.evently.dto.booking;

import com.chaitanya.evently.model.status.BookingStatus;
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
public class BookingStatusUpdateRequest {

    @NotNull(message = "Booking status is required")
    private BookingStatus status;
}
