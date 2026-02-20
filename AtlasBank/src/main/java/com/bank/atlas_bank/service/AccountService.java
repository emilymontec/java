package com.bank.atlas_bank.service;

import com.bank.atlas_bank.model.Account;
import com.bank.atlas_bank.model.Customer;
import com.bank.atlas_bank.model.Transaction;
import com.bank.atlas_bank.repository.AccountRepository;
import com.bank.atlas_bank.repository.CustomerRepository;
import com.bank.atlas_bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

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

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        return account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
    }
}
