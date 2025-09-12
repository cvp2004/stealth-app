package com.chaitanya.evently.dto.venue;

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
public class VenueResponse {
    public static final String REF_PREFIX = "ven-";
    private Long id;
    private String refId;
    private String name;
    private String address;
    private Integer capacity;
}