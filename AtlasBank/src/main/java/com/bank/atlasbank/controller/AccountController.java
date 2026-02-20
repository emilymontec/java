package com.bank.atlasbank.controller;

import com.bank.atlasbank.model.Account;
import com.bank.atlasbank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

/**
 * Controlador que agrupa las operaciones principales sobre cuentas bancarias.
 * <p>
 * Permite crear cuentas, consultar balance, depositar, retirar y transferir
 * dinero entre cuentas.
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Crea una nueva cuenta para un cliente existente.
     *
     * @param accountNumber  número único de cuenta
     * @param customerId     identificador del cliente propietario
     * @param initialBalance balance inicial opcional de la cuenta
     * @return la cuenta creada con su información persistida
     */
    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestParam String accountNumber,
            @RequestParam Long customerId,
            @RequestParam(required = false) BigDecimal initialBalance
    ) {
        Account account = accountService.createAccount(accountNumber, customerId, initialBalance);
        return ResponseEntity.ok(account);
    }

    /**
     * Consulta el balance actual de una cuenta.
     *
     * @param accountNumber número de cuenta a consultar
     * @return balance disponible en la cuenta
     */
    @GetMapping("/{accountNumber}/balance")
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
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<String> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {

        accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok("Depósito exitoso");
    }

    /**
     * Realiza un retiro desde la cuenta indicada, validando fondos suficientes.
     *
     * @param accountNumber número de cuenta desde la que se retira
     * @param amount        monto a retirar
     * @return mensaje de confirmación de la operación
     */
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<String> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {

        accountService.withdraw(accountNumber, amount);
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
