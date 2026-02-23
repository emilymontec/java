package com.bank.atlasbank.service;

import com.bank.atlasbank.model.Account;
import com.bank.atlasbank.model.Customer;
import com.bank.atlasbank.model.AccountStatus;
import com.bank.atlasbank.model.AccountType;
import com.bank.atlasbank.model.Transaction;
import com.bank.atlasbank.model.TransactionStatus;
import com.bank.atlasbank.repository.AccountRepository;
import com.bank.atlasbank.repository.CustomerRepository;
import com.bank.atlasbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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

    public static class FinancialReport {
        private final BigDecimal totalCredits;
        private final BigDecimal totalDebits;
        private final BigDecimal net;
        private final long transactionCount;
        private final LocalDateTime from;
        private final LocalDateTime to;

        public FinancialReport(BigDecimal totalCredits, BigDecimal totalDebits, BigDecimal net, long transactionCount, LocalDateTime from, LocalDateTime to) {
            this.totalCredits = totalCredits;
            this.totalDebits = totalDebits;
            this.net = net;
            this.transactionCount = transactionCount;
            this.from = from;
            this.to = to;
        }

        public BigDecimal getTotalCredits() {
            return totalCredits;
        }

        public BigDecimal getTotalDebits() {
            return totalDebits;
        }

        public BigDecimal getNet() {
            return net;
        }

        public long getTransactionCount() {
            return transactionCount;
        }

        public LocalDateTime getFrom() {
            return from;
        }

        public LocalDateTime getTo() {
            return to;
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        return authentication.getName();
    }

    @Transactional(readOnly = true)
    public FinancialReport getFinancialReport(LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();
        LocalDateTime start = d.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<Transaction> transactions = transactionRepository.findByTimestampBetween(start, end);
        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            BigDecimal amount = t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO;
            String type = t.getType();
            if ("DEPOSIT".equals(type) || "TRANSFER_IN".equals(type) || "INTEREST".equals(type) || "REVERSAL_WITHDRAW".equals(type) || "REVERSAL_EXTERNAL_TRANSFER_OUT".equals(type)) {
                totalCredits = totalCredits.add(amount);
            } else if ("WITHDRAW".equals(type) || "TRANSFER_OUT".equals(type) || "EXTERNAL_TRANSFER_OUT".equals(type) || "REVERSAL_DEPOSIT".equals(type) || "REVERSAL_INTEREST".equals(type)) {
                totalDebits = totalDebits.add(amount);
            }
        }
        BigDecimal net = totalCredits.subtract(totalDebits);
        return new FinancialReport(totalCredits, totalDebits, net, transactions.size(), start, end);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getGlobalTransactions(LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();
        LocalDateTime start = d.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return transactionRepository.findByTimestampBetween(start, end);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
    }

    private void ensureAccountIsActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("La cuenta no está activa");
        }
    }

    private void ensureSameCurrency(Account origin, Account destination) {
        if (!origin.getCurrency().equals(destination.getCurrency())) {
            throw new RuntimeException("Las cuentas deben tener la misma moneda");
        }
    }

    private AccountType parseAccountType(String type) {
        if (type == null || type.isBlank()) {
            return AccountType.SAVINGS;
        }
        String value = type.trim().toUpperCase();
        switch (value) {
            case "SAVINGS":
            case "AHORROS":
                return AccountType.SAVINGS;
            case "CHECKING":
            case "CORRIENTE":
                return AccountType.CHECKING;
            case "BUSINESS":
            case "EMPRESARIAL":
                return AccountType.BUSINESS;
            default:
                throw new IllegalArgumentException("Tipo de cuenta inválido");
        }
    }

    private AccountStatus parseAccountStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        String value = status.trim().toUpperCase();
        switch (value) {
            case "ACTIVE":
            case "ACTIVA":
                return AccountStatus.ACTIVE;
            case "FROZEN":
            case "CONGELADA":
                return AccountStatus.FROZEN;
            case "CLOSED":
            case "CERRADA":
                return AccountStatus.CLOSED;
            default:
                throw new IllegalArgumentException("Estado de cuenta inválido");
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "USD";
        }
        String value = currency.trim().toUpperCase();
        if (value.length() != 3) {
            throw new IllegalArgumentException("La moneda debe tener 3 caracteres");
        }
        return value;
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.valueOf(System.currentTimeMillis());
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        return accountNumber;
    }

    private void checkDailyDebitLimit(Account account, BigDecimal amountToDebit) {
        BigDecimal limit = account.getDailyDebitLimit() != null ? account.getDailyDebitLimit() : BigDecimal.ZERO;
        if (limit.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<Transaction> today = transactionRepository.findByAccountAndTimestampBetween(account, start, end);
        Set<String> debitTypes = Set.of("WITHDRAW", "TRANSFER_OUT", "EXTERNAL_TRANSFER_OUT");
        BigDecimal debitedToday = today.stream()
                .filter(t -> debitTypes.contains(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal projected = debitedToday.add(amountToDebit);
        if (projected.compareTo(limit) > 0) {
            throw new RuntimeException("Se excede el límite diario de débito");
        }
    }

    /**
     * Transfiere fondos hacia una cuenta externa (otra entidad).
     *
    */
    @Transactional
    public void externalTransfer(String fromAccount, BigDecimal amount, String requestId, String source) {
        validateAmount(amount);
        if (requestId != null && transactionRepository.findByRequestId(requestId).isPresent()) {
            return;
        }
        Account origin = accountRepository.findWithLockByAccountNumber(fromAccount)
                .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));
        ensureAccountIsActive(origin);
        checkDailyDebitLimit(origin, amount);

        BigDecimal originBalance = origin.getBalance() != null ? origin.getBalance() : BigDecimal.ZERO;
        if (originBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Fondos insuficientes");
        }

        BigDecimal newOriginBalance = originBalance.subtract(amount);
        if (newOriginBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El balance no puede ser negativo");
        }
        origin.setBalance(newOriginBalance);

        String performedBy = getCurrentUsername();

        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type("EXTERNAL_TRANSFER_OUT")
                        .timestamp(LocalDateTime.now())
                        .requestId(requestId)
                        .previousBalance(originBalance)
                        .newBalance(newOriginBalance)
                        .performedBy(performedBy)
                        .source(source)
                        .status(TransactionStatus.COMPLETED)
                        .account(origin)
                        .build()
        );
    }

    /**
     * Crea una nueva cuenta asociada a un cliente existente.
     *
     * @param accountNumber  número único de cuenta
     * @param customerId     identificador del cliente propietario
     * @param initialBalance balance inicial; si es nulo se asume cero
     * @return cuenta persistida en la base de datos
     */
    @Transactional
    public Account createAccount(Long customerId, BigDecimal initialBalance, String type, String currency, BigDecimal interestRate) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal rate = interestRate != null ? interestRate : BigDecimal.ZERO;
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tasa de interés no puede ser negativa");
        }
        AccountType accountType = parseAccountType(type);
        String currencyCode = normalizeCurrency(currency);
        String accountNumber = generateUniqueAccountNumber();
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .balance(balance)
                .type(accountType)
                .status(AccountStatus.ACTIVE)
                .currency(currencyCode)
                .interestRate(rate)
                .dailyDebitLimit(new BigDecimal("4000000.00"))
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
    public void deposit(String accountNumber, BigDecimal amount, String requestId, String source) {
        validateAmount(amount);
        if (requestId != null && transactionRepository.findByRequestId(requestId).isPresent()) {
            return;
        }
        Account account = accountRepository.findWithLockByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        ensureAccountIsActive(account);

        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El balance no puede ser negativo");
        }
        account.setBalance(newBalance);

        String performedBy = getCurrentUsername();

        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type("DEPOSIT")
                        .timestamp(LocalDateTime.now())
                        .requestId(requestId)
                        .previousBalance(currentBalance)
                        .newBalance(newBalance)
                        .performedBy(performedBy)
                        .source(source)
                        .status(TransactionStatus.COMPLETED)
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
    public void withdraw(String accountNumber, BigDecimal amount, String requestId, String source) {
        validateAmount(amount);
        if (requestId != null && transactionRepository.findByRequestId(requestId).isPresent()) {
            return;
        }
        Account account = accountRepository.findWithLockByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        ensureAccountIsActive(account);
        checkDailyDebitLimit(account, amount);

        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

        if (currentBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Fondos insuficientes");
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El balance no puede ser negativo");
        }
        account.setBalance(newBalance);

        String performedBy = getCurrentUsername();

        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type("WITHDRAW")
                        .timestamp(LocalDateTime.now())
                        .requestId(requestId)
                        .previousBalance(currentBalance)
                        .newBalance(newBalance)
                        .performedBy(performedBy)
                        .source(source)
                        .status(TransactionStatus.COMPLETED)
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
    public void transfer(String fromAccount, String toAccount, BigDecimal amount, String requestId, String source) {
        validateAmount(amount);
        if (requestId != null && transactionRepository.findByRequestId(requestId + "-OUT").isPresent()) {
            return;
        }
        String a1 = fromAccount.compareTo(toAccount) <= 0 ? fromAccount : toAccount;
        String a2 = fromAccount.compareTo(toAccount) <= 0 ? toAccount : fromAccount;
        Account first = accountRepository.findWithLockByAccountNumber(a1)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        Account second = accountRepository.findWithLockByAccountNumber(a2)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        Account origin = first.getAccountNumber().equals(fromAccount) ? first : second;
        Account destination = first.getAccountNumber().equals(toAccount) ? first : second;

        ensureAccountIsActive(origin);
        ensureAccountIsActive(destination);
        ensureSameCurrency(origin, destination);
        checkDailyDebitLimit(origin, amount);

        BigDecimal originBalance = origin.getBalance() != null ? origin.getBalance() : BigDecimal.ZERO;

        if (originBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Fondos insuficientes");
        }

        BigDecimal newOriginBalance = originBalance.subtract(amount);
        if (newOriginBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El balance no puede ser negativo");
        }
        origin.setBalance(newOriginBalance);

        BigDecimal destinationBalance = destination.getBalance() != null ? destination.getBalance() : BigDecimal.ZERO;
        BigDecimal newDestinationBalance = destinationBalance.add(amount);
        if (newDestinationBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El balance no puede ser negativo");
        }
        destination.setBalance(newDestinationBalance);

        String performedBy = getCurrentUsername();

        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type("TRANSFER_OUT")
                        .timestamp(LocalDateTime.now())
                        .requestId(requestId == null ? null : requestId + "-OUT")
                        .previousBalance(originBalance)
                        .newBalance(newOriginBalance)
                        .performedBy(performedBy)
                        .source(source)
                        .status(TransactionStatus.COMPLETED)
                        .account(origin)
                        .build()
        );

        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type("TRANSFER_IN")
                        .timestamp(LocalDateTime.now())
                        .requestId(requestId == null ? null : requestId + "-IN")
                        .previousBalance(destinationBalance)
                        .newBalance(newDestinationBalance)
                        .performedBy(performedBy)
                        .source(source)
                        .status(TransactionStatus.COMPLETED)
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

    @Transactional
    public void changeStatus(String accountNumber, String status) {
        Account account = accountRepository.findWithLockByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        AccountStatus newStatus = parseAccountStatus(status);
        account.setStatus(newStatus);
    }

    @Transactional
    public void applyInterest(String accountNumber, String requestId, String source) {
        if (requestId != null && transactionRepository.findByRequestId(requestId).isPresent()) {
            return;
        }
        Account account = accountRepository.findWithLockByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        ensureAccountIsActive(account);
        BigDecimal rate = account.getInterestRate() != null ? account.getInterestRate() : BigDecimal.ZERO;
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        if (currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal interest = currentBalance.multiply(rate);
        BigDecimal newBalance = currentBalance.add(interest);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El balance no puede ser negativo");
        }
        account.setBalance(newBalance);

        String performedBy = getCurrentUsername();
        transactionRepository.save(
                Transaction.builder()
                        .amount(interest)
                        .type("INTEREST")
                        .timestamp(LocalDateTime.now())
                        .requestId(requestId)
                        .previousBalance(currentBalance)
                        .newBalance(newBalance)
                        .performedBy(performedBy)
                        .source(source)
                        .status(TransactionStatus.COMPLETED)
                        .account(account)
                        .build()
        );
    }

    @Transactional
    public void reverseTransaction(Long transactionId, String requestId, String source) {
        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
        if (original.getStatus() != TransactionStatus.COMPLETED) {
            throw new RuntimeException("Solo se pueden revertir transacciones completadas");
        }
        String reverseRequestId = requestId != null ? requestId : "REVERSE-" + transactionId;
        if (transactionRepository.findByRequestId(reverseRequestId).isPresent()) {
            return;
        }
        Account account = accountRepository.findWithLockByAccountNumber(original.getAccount().getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        String type = original.getType();
        BigDecimal amount = original.getAmount() != null ? original.getAmount() : BigDecimal.ZERO;
        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance;
        String reversalType;
        if ("DEPOSIT".equals(type)) {
            newBalance = currentBalance.subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("No se puede revertir el depósito, saldo insuficiente");
            }
            reversalType = "REVERSAL_DEPOSIT";
        } else if ("WITHDRAW".equals(type)) {
            newBalance = currentBalance.add(amount);
            reversalType = "REVERSAL_WITHDRAW";
        } else if ("EXTERNAL_TRANSFER_OUT".equals(type)) {
            newBalance = currentBalance.add(amount);
            reversalType = "REVERSAL_EXTERNAL_TRANSFER_OUT";
        } else if ("INTEREST".equals(type)) {
            newBalance = currentBalance.subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("No se puede revertir el interés, saldo insuficiente");
            }
            reversalType = "REVERSAL_INTEREST";
        } else {
            throw new RuntimeException("Este tipo de transacción no admite reversión automática");
        }
        account.setBalance(newBalance);
        String performedBy = getCurrentUsername();
        transactionRepository.save(
                Transaction.builder()
                        .amount(amount)
                        .type(reversalType)
                        .timestamp(LocalDateTime.now())
                        .requestId(reverseRequestId)
                        .previousBalance(currentBalance)
                        .newBalance(newBalance)
                        .performedBy(performedBy)
                        .source(source)
                        .status(TransactionStatus.COMPLETED)
                        .account(account)
                        .build()
        );
    }
}
