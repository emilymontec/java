package com.bank.atlasbank.controller;

import com.bank.atlasbank.exception.GlobalExceptionHandler;
import com.bank.atlasbank.model.Customer;
import com.bank.atlasbank.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerControllerTest {

    private MockMvc mockMvc;

    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository = org.mockito.Mockito.mock(CustomerRepository.class);
        CustomerController controller = new CustomerController(customerRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createCustomerPersistsAndReturnsCustomer() throws Exception {
        when(customerRepository.findByDocumentId("12345")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        String body = "{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"documentId\":\"12345\",\"status\":\"PENDING\"}";

        mockMvc.perform(post("/admin/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createCustomerWithExistingDocumentReturnsBadRequest() throws Exception {
        Customer existing = Customer.builder()
                .id(1L)
                .name("Existing")
                .email("existing@example.com")
                .documentId("12345")
                .status("ACTIVE")
                .build();
        when(customerRepository.findByDocumentId("12345")).thenReturn(Optional.of(existing));

        String body = "{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"documentId\":\"12345\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/admin/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomerWithInvalidNameReturnsBadRequest() throws Exception {
        String body = "{\"name\":\"\",\"email\":\"alice@example.com\",\"documentId\":\"99999\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/admin/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomerWithShortDocumentReturnsBadRequest() throws Exception {
        String body = "{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"documentId\":\"123\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/admin/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCustomerModifiesExistingCustomer() throws Exception {
        Customer existing = Customer.builder()
                .id(1L)
                .name("Old Name")
                .email("old@example.com")
                .documentId("12345")
                .status("ACTIVE")
                .build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.findByDocumentId("99999")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String body = "{\"name\":\"New Name\",\"email\":\"new@example.com\",\"documentId\":\"99999\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(put("/admin/customers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.documentId").value("99999"));
    }

    @Test
    void updateStatusNormalizesAndPersistsStatus() throws Exception {
        Customer existing = Customer.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .documentId("12345")
                .status("ACTIVE")
                .build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/admin/customers/{id}/status", 1L)
                        .param("status", "blocked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("BLOCKED");
    }
}
