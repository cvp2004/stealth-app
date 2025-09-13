package com.chaitanya.evently.dto.event;

import com.chaitanya.evently.model.status.EventStatus;
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
public class EventStatusUpdateRequest {

    @NotNull(message = "Event status is required")
    private EventStatus status;
}
