package com.chaitanya.evently.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateResponse {

    private String reservationId;
    private BigDecimal totalAmount;
    private Instant expiresAt;
    private String message;
    private boolean success;
}
