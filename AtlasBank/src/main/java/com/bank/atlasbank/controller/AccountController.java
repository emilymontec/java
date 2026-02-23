package com.bank.atlasbank.controller;

import com.bank.atlasbank.model.Account;
import com.bank.atlasbank.model.Transaction;
import com.bank.atlasbank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

/**
 * Controlador que agrupa las operaciones principales sobre cuentas bancarias.
 * <p>
 * Permite crear cuentas, consultar balance, depositar, retirar y transferir
 * dinero entre cuentas.
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(
            @RequestParam Long customerId,
            @RequestParam(required = false) BigDecimal initialBalance,
            @RequestParam String type,
            @RequestParam String currency,
            @RequestParam(required = false) BigDecimal interestRate
    ) {
        Account account = accountService.createAccount(customerId, initialBalance, type, currency, interestRate);
        return ResponseEntity.ok(account);
    }

    /**
     * Consulta el balance actual de una cuenta.
     *
     * @param accountNumber número de cuenta a consultar
     * @return balance disponible en la cuenta
     */
    @GetMapping("/accounts/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    /**
     * Realiza un depósito en la cuenta indicada.
     *
     * @param accountNumber número de cuenta destino del depósito
     * @param amount        monto a depositar
     * @return mensaje de confirmación de la operación
     */
    @PostMapping("/transactions/deposit")
    public ResponseEntity<String> deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String requestId,
            HttpServletRequest request) {

        String source = request.getRemoteAddr();
        accountService.deposit(accountNumber, amount, requestId, source);
        return ResponseEntity.ok("Depósito exitoso");
    }

    /**
     * Realiza un retiro desde la cuenta indicada, validando fondos suficientes.
     *
     * @param accountNumber número de cuenta desde la que se retira
     * @param amount        monto a retirar
     * @return mensaje de confirmación de la operación
     */
    @PostMapping("/transactions/withdraw")
    public ResponseEntity<String> withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String requestId,
            HttpServletRequest request) {

        String source = request.getRemoteAddr();
        accountService.withdraw(accountNumber, amount, requestId, source);
        return ResponseEntity.ok("Retiro exitoso");
    }

    /**
     * Transfiere fondos entre dos cuentas existentes.
     *
     * @param fromAccount cuenta origen
     * @param toAccount   cuenta destino
     * @param amount      monto a transferir
     * @return mensaje de confirmación de la operación
     */
    @PostMapping("/transactions/transfer/internal")
    public ResponseEntity<String> transfer(
            @RequestParam String fromAccount,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String requestId,
            HttpServletRequest request
    ) {

        String source = request.getRemoteAddr();
        accountService.transfer(fromAccount, toAccount, amount, requestId, source);
        return ResponseEntity.ok("Transferencia realizada");
    }

    @PostMapping("/transactions/transfer/external")
    public ResponseEntity<String> externalTransfer(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String requestId,
            HttpServletRequest request
    ) {
        String source = request.getRemoteAddr();
        accountService.externalTransfer(accountNumber, amount, requestId, source);
        return ResponseEntity.ok("Transferencia externa realizada");
    }

    @PostMapping("/admin/accounts/{accountNumber}/status")
    public ResponseEntity<String> changeStatus(
            @PathVariable String accountNumber,
            @RequestParam String status
    ) {
        accountService.changeStatus(accountNumber, status);
        return ResponseEntity.ok("Estado de cuenta actualizado");
    }

    @PostMapping("/admin/accounts/{accountNumber}/interest/apply")
    public ResponseEntity<String> applyInterest(
                                                @PathVariable String accountNumber,
                                                @RequestParam(required = false) String requestId,
                                                HttpServletRequest request) {
        String source = request.getRemoteAddr();
        accountService.applyInterest(accountNumber, requestId, source);
        return ResponseEntity.ok("Interés aplicado");
    }

    @GetMapping("/admin/transactions")
    public ResponseEntity<List<Transaction>> getGlobalTransactions(@RequestParam(required = false) LocalDate date) {
        List<Transaction> transactions = accountService.getGlobalTransactions(date);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/admin/report")
    public ResponseEntity<AccountService.FinancialReport> getFinancialReport(@RequestParam(required = false) LocalDate date) {
        AccountService.FinancialReport report = accountService.getFinancialReport(date);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/admin/transactions/{transactionId}/revert")
    public ResponseEntity<String> reverseTransaction(
            @PathVariable Long transactionId,
            @RequestParam(required = false) String requestId,
            HttpServletRequest request) {
        String source = request.getRemoteAddr();
        accountService.reverseTransaction(transactionId, requestId, source);
        return ResponseEntity.ok("Operación revertida");
    }
}
