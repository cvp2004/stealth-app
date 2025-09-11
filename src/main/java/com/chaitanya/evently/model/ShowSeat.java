package com.chaitanya.evently.model;

import com.chaitanya.evently.model.base.BaseEntity;
import com.chaitanya.evently.model.status.ShowSeatStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
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
@Table(name = "show_seats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_show_seat", columnNames = { "show_id", "seat_id" })
})
public class ShowSeat extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShowSeatStatus status = ShowSeatStatus.AVAILABLE;

}
