package com.chaitanya.evently.dto.event.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "title is required")
    @NotBlank(message = "title cannot be blank")
    @Size(min = 1, max = 255, message = "title must be between 1 and 255 characters")
    private String title;

    @Size(max = 2000, message = "description must be at most 2000 characters")
    private String description;

    @Size(max = 50, message = "category must be at most 50 characters")
    private String category;
}
