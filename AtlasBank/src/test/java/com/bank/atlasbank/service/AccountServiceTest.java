package com.bank.atlasbank.service;

import com.bank.atlasbank.model.Account;
import com.bank.atlasbank.model.AccountStatus;
import com.bank.atlasbank.model.AccountType;
import com.bank.atlasbank.model.Customer;
import com.bank.atlasbank.model.Transaction;
import com.bank.atlasbank.model.TransactionStatus;
import com.bank.atlasbank.repository.AccountRepository;
import com.bank.atlasbank.repository.CustomerRepository;
import com.bank.atlasbank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private CustomerRepository customerRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        customerRepository = mock(CustomerRepository.class);
        accountService = new AccountService(accountRepository, transactionRepository, customerRepository);
    }

    @Test
    void depositUpdatesBalanceAndCreatesTransaction() {
        Account account = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("100.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .build();
        when(accountRepository.findWithLockByAccountNumber("ACC-001")).thenReturn(Optional.of(account));
        when(transactionRepository.findByRequestId("REQ-1")).thenReturn(Optional.empty());

        accountService.deposit("ACC-001", new BigDecimal("50.00"), "REQ-1", "test");

        assertThat(account.getBalance()).isEqualByComparingTo("150.00");

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction saved = txCaptor.getValue();
        assertThat(saved.getType()).isEqualTo("DEPOSIT");
        assertThat(saved.getAmount()).isEqualByComparingTo("50.00");
        assertThat(saved.getPreviousBalance()).isEqualByComparingTo("100.00");
        assertThat(saved.getNewBalance()).isEqualByComparingTo("150.00");
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void withdrawWithInsufficientFundsThrows() {
        Account account = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("30.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .build();
        when(accountRepository.findWithLockByAccountNumber("ACC-001")).thenReturn(Optional.of(account));
        when(transactionRepository.findByRequestId("REQ-2")).thenReturn(Optional.empty());
        when(transactionRepository.findByAccountAndTimestampBetween(eq(account), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> accountService.withdraw("ACC-001", new BigDecimal("50.00"), "REQ-2", "test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Fondos insuficientes");
    }

    @Test
    void withdrawExceedingDailyLimitThrows() {
        Account account = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("500.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("100.00"))
                .build();
        when(accountRepository.findWithLockByAccountNumber("ACC-001")).thenReturn(Optional.of(account));
        when(transactionRepository.findByRequestId("REQ-3")).thenReturn(Optional.empty());
        Transaction previous = Transaction.builder()
                .amount(new BigDecimal("80.00"))
                .type("WITHDRAW")
                .build();
        when(transactionRepository.findByAccountAndTimestampBetween(eq(account), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(previous));

        assertThatThrownBy(() -> accountService.withdraw("ACC-001", new BigDecimal("30.00"), "REQ-3", "test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Se excede el límite diario de débito");
    }

    @Test
    void transferBetweenAccountsUpdatesBalancesAndCreatesTransactions() {
        Account origin = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("200.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .build();
        Account destination = Account.builder()
                .id(2L)
                .accountNumber("ACC-002")
                .balance(new BigDecimal("50.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .build();

        when(accountRepository.findWithLockByAccountNumber("ACC-001")).thenReturn(Optional.of(origin));
        when(accountRepository.findWithLockByAccountNumber("ACC-002")).thenReturn(Optional.of(destination));
        when(transactionRepository.findByRequestId("TR-1-OUT")).thenReturn(Optional.empty());
        when(transactionRepository.findByAccountAndTimestampBetween(eq(origin), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        accountService.transfer("ACC-001", "ACC-002", new BigDecimal("50.00"), "TR-1", "test");

        assertThat(origin.getBalance()).isEqualByComparingTo("150.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void transferWithDifferentCurrenciesThrows() {
        Account origin = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("200.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .build();
        Account destination = Account.builder()
                .id(2L)
                .accountNumber("ACC-002")
                .balance(new BigDecimal("50.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("EUR")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .build();

        when(accountRepository.findWithLockByAccountNumber("ACC-001")).thenReturn(Optional.of(origin));
        when(accountRepository.findWithLockByAccountNumber("ACC-002")).thenReturn(Optional.of(destination));
        when(transactionRepository.findByRequestId("TR-2-OUT")).thenReturn(Optional.empty());
        when(transactionRepository.findByAccountAndTimestampBetween(eq(origin), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> accountService.transfer("ACC-001", "ACC-002", new BigDecimal("10.00"), "TR-2", "test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Las cuentas deben tener la misma moneda");
    }

    @Test
    void transferIsIdempotentWhenOutTransactionAlreadyExists() {
        when(transactionRepository.findByRequestId("TR-3-OUT")).thenReturn(Optional.of(Transaction.builder().id(99L).build()));

        accountService.transfer("ACC-001", "ACC-002", new BigDecimal("10.00"), "TR-3", "test");

        verify(accountRepository, never()).findWithLockByAccountNumber(any());
    }

    @Test
    void externalTransferIsIdempotentWhenRequestAlreadyProcessed() {
        when(transactionRepository.findByRequestId("REQ-EXT")).thenReturn(Optional.of(Transaction.builder().id(1L).build()));

        accountService.externalTransfer("ACC-001", new BigDecimal("10.00"), "REQ-EXT", "test");

        verify(accountRepository, never()).findWithLockByAccountNumber(any());
    }

    @Test
    void changeStatusUpdatesAccountStatus() {
        Account account = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("100.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .build();
        when(accountRepository.findWithLockByAccountNumber("ACC-001")).thenReturn(Optional.of(account));

        accountService.changeStatus("ACC-001", "CERRADA");

        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void applyInterestWithPositiveRateCreatesTransactionAndUpdatesBalance() {
        Account account = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("100.00"))
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(new BigDecimal("0.10"))
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .build();
        when(accountRepository.findWithLockByAccountNumber("ACC-001")).thenReturn(Optional.of(account));
        when(transactionRepository.findByRequestId("INT-1")).thenReturn(Optional.empty());

        accountService.applyInterest("ACC-001", "INT-1", "test");

        assertThat(account.getBalance()).isEqualByComparingTo("110.00");

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction saved = txCaptor.getValue();
        assertThat(saved.getType()).isEqualTo("INTEREST");
        assertThat(saved.getAmount()).isEqualByComparingTo("10.00");
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void getBalanceReturnsZeroWhenNoBalanceSet() {
        Account account = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(null)
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .interestRate(BigDecimal.ZERO)
                .dailyDebitLimit(new BigDecimal("1000.00"))
                .build();
        when(accountRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.of(account));

        BigDecimal result = accountService.getBalance("ACC-001");

        assertThat(result).isEqualByComparingTo("0.00");
    }
}
