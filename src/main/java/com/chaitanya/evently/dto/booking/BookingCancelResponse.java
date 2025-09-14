package com.chaitanya.evently.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelResponse {

    private Long bookingId;
    private String message;
    private Boolean success;
    private BigDecimal refundAmount;
}
