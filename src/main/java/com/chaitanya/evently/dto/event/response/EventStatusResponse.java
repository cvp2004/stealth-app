package com.chaitanya.evently.dto.event.response;

import com.chaitanya.evently.model.status.EventStatus;
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
public class EventStatusResponse {
    private Long id;
    private String refId;
    private String title;
    private EventStatus status;
}
