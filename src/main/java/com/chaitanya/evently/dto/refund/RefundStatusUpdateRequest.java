package com.chaitanya.evently.dto.refund;

import com.chaitanya.evently.model.status.RefundStatus;
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
public class RefundStatusUpdateRequest {

    @NotNull(message = "Refund status is required")
    private RefundStatus status;
}
