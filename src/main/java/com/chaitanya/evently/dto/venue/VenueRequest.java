package com.chaitanya.evently.dto.venue;

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
public class VenueRequest {
    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 chars")
    private String name;

    @Size(max = 1000, message = "address must be at most 1000 chars")
    private String address;

}
