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
@Table(name = "emails")
public class Email extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false)
    private EmailType emailType;

    @Column(name = "email_subject", nullable = false, length = 500)
    private String emailSubject;

    @Column(name = "email_body", nullable = false, columnDefinition = "TEXT")
    private String emailBody;

    @Builder.Default
    @Column(name = "email_sent", nullable = false)
    private Boolean emailSent = false;

    @Column(name = "sent_at")
    private java.time.Instant sentAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public enum EmailType {
        CANCEL_BOOKING,
        BOOKING_CONFIRMATION
    }
}
