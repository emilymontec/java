package com.bank.atlasbank.repository;

import com.bank.atlasbank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import com.bank.atlasbank.model.Account;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Transaction.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAndTimestampBetween(Account account, LocalDateTime start, LocalDateTime end);
    Optional<Transaction> findByRequestId(String requestId);
    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
