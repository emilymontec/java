package com.bank.atlasbank.repository;

import com.bank.atlasbank.AtlasBankApplication;
import com.bank.atlasbank.model.Account;
import com.bank.atlasbank.model.AccountStatus;
import com.bank.atlasbank.model.AccountType;
import com.bank.atlasbank.model.Customer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRepositoryTest {

    private static ConfigurableApplicationContext context;
    private static AccountRepository accountRepository;
    private static CustomerRepository customerRepository;

    @BeforeAll
    static void init() {
        context = SpringApplication.run(AtlasBankApplication.class);
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
    void findByAccountNumberReturnsPersistedAccount() {
        Customer customer = customerRepository.save(
                Customer.builder()
                        .name("Alice")
                        .email("alice@example.com")
                        .documentId("DOC-456")
                        .status("ACTIVE")
                        .build()
        );

        Account account = Account.builder()
                .accountNumber("ACC-001")
                .balance(new BigDecimal("100.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .customer(customer)
                .build();

        accountRepository.save(account);

        Optional<Account> result = accountRepository.findByAccountNumber("ACC-001");

        assertThat(result).isPresent();
        assertThat(result.get().getAccountNumber()).isEqualTo("ACC-001");
        assertThat(result.get().getCustomer().getId()).isEqualTo(customer.getId());
    }
}
