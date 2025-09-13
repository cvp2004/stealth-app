package com.chaitanya.evently.dto.show;

import com.chaitanya.evently.model.status.ShowStatus;
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
public class ShowStatusUpdateRequest {

    @NotNull(message = "Show status is required")
    private ShowStatus status;
}
