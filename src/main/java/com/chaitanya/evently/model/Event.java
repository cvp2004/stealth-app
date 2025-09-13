package com.chaitanya.evently.model;


import com.chaitanya.evently.model.base.BaseEntity;
import com.chaitanya.evently.model.status.EventStatus;

import jakarta.persistence.*;
import lombok.*;

/**
 * Event entity representing an event in the system
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Event extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status = EventStatus.CREATED;
}
