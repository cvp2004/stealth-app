package com.chaitanya.evently.dto.seat.map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

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
public class SectionDefinition {
    @NotBlank(message = "section is required")
    @Size(max = 100, message = "section must be at most 100 chars")
    private String section;

    @NotEmpty(message = "rows are required")
    @Valid
    private List<RowDefinition> rows;
}
