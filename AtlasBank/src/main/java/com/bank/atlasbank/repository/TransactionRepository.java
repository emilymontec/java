package com.bank.atlasbank.repository;

import com.bank.atlasbank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Transaction.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
