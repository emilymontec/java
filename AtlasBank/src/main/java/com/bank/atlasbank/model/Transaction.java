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

    private String type; // DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT

    private LocalDateTime timestamp;

    @ManyToOne
    private Account account;
}
