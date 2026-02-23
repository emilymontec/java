package com.bank.atlasbank.controller;

import com.bank.atlasbank.exception.GlobalExceptionHandler;
import com.bank.atlasbank.model.Account;
import com.bank.atlasbank.model.AccountStatus;
import com.bank.atlasbank.model.AccountType;
import com.bank.atlasbank.model.Customer;
import com.bank.atlasbank.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTest {

    private MockMvc mockMvc;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = org.mockito.Mockito.mock(AccountService.class);
        AccountController controller = new AccountController(accountService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createAccountReturnsPersistedAccount() throws Exception {
        Customer customer = Customer.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .documentId("12345")
                .status("ACTIVE")
                .build();
        Account account = Account.builder()
                .id(10L)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("100.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .customer(customer)
                .build();
        when(accountService.createAccount(1L, null, "SAVINGS", "USD", null)).thenReturn(account);

        mockMvc.perform(post("/accounts")
                        .param("customerId", "1")
                        .param("type", "SAVINGS")
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.accountNumber").value("ACC-001"));
    }

    @Test
    void transferInternalReturnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/transactions/transfer/internal")
                        .param("fromAccount", "ACC-001")
                        .param("toAccount", "ACC-002")
                        .param("amount", "50.00"))
                .andExpect(status().isOk())
                .andExpect(content().string("Transferencia realizada"));

        then(accountService).should().transfer(eq("ACC-001"), eq("ACC-002"), eq(new BigDecimal("50.00")), any(), any());
    }

    @Test
    void externalTransferReturnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/transactions/transfer/external")
                        .param("accountNumber", "ACC-001")
                        .param("amount", "75.00"))
                .andExpect(status().isOk())
                .andExpect(content().string("Transferencia externa realizada"));

        then(accountService).should().externalTransfer(eq("ACC-001"), eq(new BigDecimal("75.00")), any(), any());
    }

    @Test
    void transferInternalWithBusinessErrorReturnsBadRequest() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Fondos insuficientes"))
                .when(accountService)
                .transfer(eq("ACC-001"), eq("ACC-002"), eq(new BigDecimal("200.00")), any(), any());

        mockMvc.perform(post("/transactions/transfer/internal")
                        .param("fromAccount", "ACC-001")
                        .param("toAccount", "ACC-002")
                        .param("amount", "200.00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Fondos insuficientes"));
    }

    @Test
    void changeStatusPropagatesServiceErrorAsBadRequest() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Estado de cuenta inválido"))
                .when(accountService)
                .changeStatus("ACC-001", "UNKNOWN");

        mockMvc.perform(post("/admin/accounts/{accountNumber}/status", "ACC-001")
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Estado de cuenta inválido"));
    }

    @Test
    void changeStatusUpdatesAccountStatus() throws Exception {
        mockMvc.perform(post("/admin/accounts/{accountNumber}/status", "ACC-001")
                        .param("status", "FROZEN"))
                .andExpect(status().isOk())
                .andExpect(content().string("Estado de cuenta actualizado"));

        then(accountService).should().changeStatus("ACC-001", "FROZEN");
    }

    @Test
    void getBalanceReturnsCurrentBalance() throws Exception {
        when(accountService.getBalance("ACC-001")).thenReturn(new BigDecimal("150.00"));

        mockMvc.perform(get("/accounts/{accountNumber}/balance", "ACC-001"))
                .andExpect(status().isOk())
                .andExpect(content().string("150.00"));
    }
}
