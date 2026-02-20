package com.bank.atlas_bank.controller;

import com.bank.atlas_bank.model.Account;
import com.bank.atlas_bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestParam String accountNumber,
            @RequestParam Long customerId,
            @RequestParam(required = false) BigDecimal initialBalance
    ) {
        Account account = accountService.createAccount(accountNumber, customerId, initialBalance);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<String> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {

        accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok("Depósito exitoso");
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<String> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {

        accountService.withdraw(accountNumber, amount);
        return ResponseEntity.ok("Retiro exitoso");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestParam String fromAccount,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount
    ) {

        accountService.transfer(fromAccount, toAccount, amount);
        return ResponseEntity.ok("Transferencia realizada");
    }
}
