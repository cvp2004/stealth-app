package com.chaitanya.evently.dto.payment;

import com.chaitanya.evently.model.status.PaymentStatus;
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
public class PaymentStatusUpdateRequest {

    @NotNull(message = "Payment status is required")
    private PaymentStatus status;
}
