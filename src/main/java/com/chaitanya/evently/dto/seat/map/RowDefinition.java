package com.chaitanya.evently.dto.seat.map;

import jakarta.validation.constraints.Min;
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
public class RowDefinition {
    @NotBlank(message = "rowNo is required")
    @Size(max = 50, message = "rowNo must be at most 50 chars")
    private String rowNo;

    @NotNull(message = "seatCount is required")
    @Min(value = 1, message = "seatCount must be at least 1")
    private Integer seatCount;
}
