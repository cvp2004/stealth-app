package com.chaitanya.evently.model;

import com.chaitanya.evently.model.base.BaseEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_seat_unique", columnNames = { "venue_id", "section", "row", "seat_number" })
})
public class Seat extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "section")
    private String section;

    @Column(name = "row")
    private String row;

    @Column(name = "seat_number")
    private String seatNumber;

}
