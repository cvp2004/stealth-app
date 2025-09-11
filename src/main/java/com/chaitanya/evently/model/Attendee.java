package com.chaitanya.evently.model;

import jakarta.persistence.*;

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
@Table(name = "attendees")
public class Attendee extends BaseEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "booked_seat_id", nullable = false, unique = true)
    private BookedSeat bookedSeat;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "age")
    private Integer age;
}
