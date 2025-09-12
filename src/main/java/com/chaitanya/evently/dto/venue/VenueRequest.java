package com.chaitanya.evently.dto.venue;

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
public class VenueRequest {
    @NotNull(message = "name is required")
    @NotBlank(message = "name cannot be blank")
    @Size(min = 1, max = 255, message = "name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "address must be at most 1000 characters")
    private String address;
}
