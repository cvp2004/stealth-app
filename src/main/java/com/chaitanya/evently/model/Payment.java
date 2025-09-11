package com.chaitanya.evently.model;

import com.chaitanya.evently.model.base.BaseEntity;
import com.chaitanya.evently.model.status.PaymentStatus;
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
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_provider_payment_id", columnNames = { "provider_payment_id" })
})
public class Payment extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_payment_id", unique = true)
    private String providerPaymentId;

}
