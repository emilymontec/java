package com.bank.atlasbank.repository;

import com.bank.atlasbank.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Customer.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
