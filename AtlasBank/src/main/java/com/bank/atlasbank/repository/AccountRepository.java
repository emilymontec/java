package com.bank.atlasbank.repository;

import com.bank.atlasbank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para operaciones de persistencia sobre cuentas.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Busca una cuenta por su número único.
     *
     * @param accountNumber número de cuenta
     * @return Optional con la cuenta encontrada o vacío si no existe
     */
    Optional<Account> findByAccountNumber(String accountNumber);
}
