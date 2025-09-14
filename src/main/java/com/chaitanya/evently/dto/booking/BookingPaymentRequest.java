package com.chaitanya.evently.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPaymentRequest {

    @NotNull(message = "Reservation ID is required")
    private String reservationId;

    @NotNull(message = "Payment amount is required")
    private BigDecimal amount;
}
