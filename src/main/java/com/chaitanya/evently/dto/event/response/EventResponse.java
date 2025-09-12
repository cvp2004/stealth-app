package com.chaitanya.evently.dto.event.response;

import com.chaitanya.evently.model.status.EventStatus;
import java.time.Instant;
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
public class EventResponse {
    private Long id;
    private String refId;
    private String title;
    private String description;
    private String category;
    private EventStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
