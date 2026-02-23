package com.bank.atlasbank.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * Entidad que registra los movimientos asociados a una cuenta.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String type;

    private LocalDateTime timestamp;

    private BigDecimal previousBalance;

    private BigDecimal newBalance;

    private String performedBy;

    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(unique = true)
    private String requestId;

    @ManyToOne
    private Account account;
}
