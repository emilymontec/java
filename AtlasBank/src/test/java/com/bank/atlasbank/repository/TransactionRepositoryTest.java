package com.bank.atlasbank.repository;

import com.bank.atlasbank.AtlasBankApplication;
import com.bank.atlasbank.model.Account;
import com.bank.atlasbank.model.AccountStatus;
import com.bank.atlasbank.model.AccountType;
import com.bank.atlasbank.model.Customer;
import com.bank.atlasbank.model.Transaction;
import com.bank.atlasbank.model.TransactionStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRepositoryTest {

    private static ConfigurableApplicationContext context;
    private static TransactionRepository transactionRepository;
    private static AccountRepository accountRepository;
    private static CustomerRepository customerRepository;

    @BeforeAll
    static void init() {
        context = SpringApplication.run(AtlasBankApplication.class);
        transactionRepository = context.getBean(TransactionRepository.class);
        accountRepository = context.getBean(AccountRepository.class);
        customerRepository = context.getBean(CustomerRepository.class);
    }

    @AfterAll
    static void destroy() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void findByRequestIdReturnsPersistedTransaction() {
        Account account = createAccountWithCustomer();

        Transaction tx = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .type("DEPOSIT")
                .timestamp(LocalDateTime.now())
                .previousBalance(BigDecimal.ZERO)
                .newBalance(new BigDecimal("50.00"))
                .performedBy("tester")
                .source("test")
                .status(TransactionStatus.COMPLETED)
                .requestId("REQ-123")
                .account(account)
                .build();

        transactionRepository.save(tx);

        Transaction found = transactionRepository.findByRequestId("REQ-123").orElseThrow();

        assertThat(found.getAmount()).isEqualByComparingTo("50.00");
        assertThat(found.getAccount().getId()).isEqualTo(account.getId());
    }

    @Test
    void findByAccountAndTimestampBetweenReturnsTransactionsWithinRange() {
        Account account = createAccountWithCustomer();
        LocalDateTime now = LocalDateTime.now();

        Transaction tx = Transaction.builder()
                .amount(new BigDecimal("20.00"))
                .type("WITHDRAW")
                .timestamp(now)
                .previousBalance(new BigDecimal("100.00"))
                .newBalance(new BigDecimal("80.00"))
                .performedBy("tester")
                .source("test")
                .status(TransactionStatus.COMPLETED)
                .account(account)
                .build();

        transactionRepository.save(tx);

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Transaction> result = transactionRepository.findByAccountAndTimestampBetween(account, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("WITHDRAW");
    }

    @Test
    void findByTimestampBetweenReturnsAllTransactionsInRange() {
        Account account = createAccountWithCustomer();
        LocalDateTime now = LocalDateTime.now();

        Transaction t1 = Transaction.builder()
                .amount(new BigDecimal("10.00"))
                .type("DEPOSIT")
                .timestamp(now.minusHours(1))
                .previousBalance(BigDecimal.ZERO)
                .newBalance(new BigDecimal("10.00"))
                .performedBy("tester")
                .source("test")
                .status(TransactionStatus.COMPLETED)
                .account(account)
                .build();

        Transaction t2 = Transaction.builder()
                .amount(new BigDecimal("5.00"))
                .type("WITHDRAW")
                .timestamp(now)
                .previousBalance(new BigDecimal("10.00"))
                .newBalance(new BigDecimal("5.00"))
                .performedBy("tester")
                .source("test")
                .status(TransactionStatus.COMPLETED)
                .account(account)
                .build();

        transactionRepository.save(t1);
        transactionRepository.save(t2);

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Transaction> result = transactionRepository.findByTimestampBetween(start, end);

        assertThat(result).hasSize(2);
    }

    private Account createAccountWithCustomer() {
        long suffix = System.nanoTime();

        Customer customer = customerRepository.save(
                Customer.builder()
                        .name("Alice")
                        .email("alice@example.com")
                        .documentId("DOC-TX-" + suffix)
                        .status("ACTIVE")
                        .build()
        );

        Account account = Account.builder()
                .accountNumber("ACC-TX-" + suffix)
                .balance(new BigDecimal("100.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .customer(customer)
                .build();

        return accountRepository.save(account);
    }
}
