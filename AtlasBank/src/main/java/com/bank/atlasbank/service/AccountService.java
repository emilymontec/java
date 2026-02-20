package com.bank.atlasbank.service;

import com.bank.atlasbank.model.Account;
import com.bank.atlasbank.model.Customer;
import com.bank.atlasbank.model.Transaction;
import com.bank.atlasbank.repository.AccountRepository;
import com.bank.atlasbank.repository.CustomerRepository;
import com.bank.atlasbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio de dominio para operaciones sobre cuentas bancarias.
 * <p>
 * Centraliza la lógica de negocio para creación de cuentas, depósitos, retiros,
 * transferencias y consulta de balance, garantizando consistencia mediante
 * transacciones.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    /**
     * Crea una nueva cuenta asociada a un cliente existente.
     *
     * @param accountNumber  número único de cuenta
     * @param customerId     identificador del cliente propietario
     * @param initialBalance balance inicial; si es nulo se asume cero
     * @return cuenta persistida en la base de datos
     */
    @Transactional
    public Account createAccount(String accountNumber, Long customerId, BigDecimal initialBalance) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        BigDecimal balance = initialBalance != null ? initialBalance : BigDecimal.ZERO;

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El balance inicial no puede ser negativo");
        }

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .balance(balance)
                .customer(customer)
                .build();

        return accountRepository.save(account);
    }

    /**
     * Aplica un depósito al balance de una cuenta y registra la transacción.
     *
     * @param accountNumber número de cuenta destino
     * @param amount        monto a depositar
     */
    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        account.setBalance(currentBalance.add(amount));

        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type("DEPOSIT")
                        .timestamp(LocalDateTime.now())
                        .account(account)
                        .build()
        );
    }

    /**
     * Realiza un retiro desde una cuenta, validando fondos suficientes y
     * registrando la transacción.
     *
     * @param accountNumber número de cuenta origen
     * @param amount        monto a retirar
     */
    @Transactional
    public void withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

        if (currentBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Fondos insuficientes");
        }

        account.setBalance(currentBalance.subtract(amount));

        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type("WITHDRAW")
                        .timestamp(LocalDateTime.now())
                        .account(account)
                        .build()
        );
    }

    /**
     * Transfiere fondos entre dos cuentas, registrando transacciones de salida y
     * entrada.
     *
     * @param fromAccount cuenta origen
     * @param toAccount   cuenta destino
     * @param amount      monto a transferir
     */
    @Transactional
    public void transfer(String fromAccount, String toAccount, BigDecimal amount) {
        Account origin = accountRepository.findByAccountNumber(fromAccount)
                .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));

        Account destination = accountRepository.findByAccountNumber(toAccount)
                .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));

        BigDecimal originBalance = origin.getBalance() != null ? origin.getBalance() : BigDecimal.ZERO;

        if (originBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Fondos insuficientes");
        }

        origin.setBalance(originBalance.subtract(amount));

        BigDecimal destinationBalance = destination.getBalance() != null ? destination.getBalance() : BigDecimal.ZERO;
        destination.setBalance(destinationBalance.add(amount));

        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type("TRANSFER_OUT")
                        .timestamp(LocalDateTime.now())
                        .account(origin)
                        .build()
        );

        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type("TRANSFER_IN")
                        .timestamp(LocalDateTime.now())
                        .account(destination)
                        .build()
        );
    }

    /**
     * Obtiene el balance actual de una cuenta.
     *
     * @param accountNumber número de cuenta a consultar
     * @return balance de la cuenta o cero si no tiene valor almacenado
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        return account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
    }
}
