package com.bank.atlasbank.transaction;

import com.bank.atlasbank.account.Account;
import com.bank.atlasbank.account.AccountService;
import com.bank.atlasbank.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountService accountService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public BankTransaction deposit(Long accountId, BigDecimal amount) {
        accountService.deposit(accountId, amount);
        Account account = accountService.findById(accountId);

        BankTransaction tx = new BankTransaction();
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setSourceAccount(account);
        return transactionRepository.save(tx);
    }

    @Transactional
    public BankTransaction withdraw(Long accountId, BigDecimal amount) {
        accountService.withdraw(accountId, amount);
        Account account = accountService.findById(accountId);

        BankTransaction tx = new BankTransaction();
        tx.setType(TransactionType.WITHDRAW);
        tx.setAmount(amount);
        tx.setSourceAccount(account);
        return transactionRepository.save(tx);
    }

    @Transactional
    public BankTransaction transfer(TransferRequest request) {
        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new BusinessException("No se puede transferir a la misma cuenta");
        }

        accountService.withdraw(request.sourceAccountId(), request.amount());
        accountService.deposit(request.targetAccountId(), request.amount());

        Account source = accountService.findById(request.sourceAccountId());
        Account target = accountService.findById(request.targetAccountId());

        BankTransaction tx = new BankTransaction();
        tx.setType(TransactionType.TRANSFER);
        tx.setAmount(request.amount());
        tx.setSourceAccount(source);
        tx.setTargetAccount(target);
        return transactionRepository.save(tx);
    }

    public List<BankTransaction> findAll() {
        return transactionRepository.findAll();
    }
}
