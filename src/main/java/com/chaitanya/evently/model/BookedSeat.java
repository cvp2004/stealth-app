package com.chaitanya.evently.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.chaitanya.evently.model.base.BaseEntity;

import lombok.AllArgsConstructor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "booked_seats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_booked_seat_unique", columnNames = { "show_seat_id" })
})
public class BookedSeat extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

}
