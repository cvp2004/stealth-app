package com.chaitanya.evently.dto.event.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class EventRequest {
    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be at most 255 chars")
    private String title;

    @Size(max = 2000, message = "description must be at most 2000 chars")
    private String description;

    @Size(max = 100, message = "category must be at most 100 chars")
    private String category;
}
