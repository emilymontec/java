package com.bank.atlasbank.repository;

import com.bank.atlasbank.AtlasBankApplication;
import com.bank.atlasbank.model.Customer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRepositoryTest {

    private static ConfigurableApplicationContext context;
    private static CustomerRepository customerRepository;

    @BeforeAll
    static void init() {
        context = SpringApplication.run(AtlasBankApplication.class);
        customerRepository = context.getBean(CustomerRepository.class);
    }

    @AfterAll
    static void destroy() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void findByDocumentIdReturnsPersistedCustomer() {
        Customer customer = Customer.builder()
                .name("Alice")
                .email("alice@example.com")
                .documentId("DOC-123")
                .status("ACTIVE")
                .build();

        customerRepository.save(customer);

        Optional<Customer> result = customerRepository.findByDocumentId("DOC-123");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice");
        assertThat(result.get().getDocumentId()).isEqualTo("DOC-123");
    }
}
