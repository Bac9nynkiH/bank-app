package test.bank.service;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import test.bank.domain.banking.BankAccount;
import test.bank.exception.BankApplicationNegativeBalanceException;
import test.bank.exception.BankApplicationNotFoundException;
import test.bank.repository.BankAccountRepository;
import test.bank.service.interfaces.AccountManagementService;
import test.bank.service.interfaces.AccountNumberGeneratorService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountManagementServiceImpl implements AccountManagementService {
    private final BankAccountRepository bankAccountRepository;
    private final AccountNumberGeneratorService accountNumberGeneratorService;
    @Override
    public BankAccount createBankAccount(BigDecimal initialBalance) {
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BankApplicationNegativeBalanceException("Balance cannot be negative");
        }

        var bankAccount = new BankAccount();
        bankAccount.setBalance(initialBalance);
        bankAccount.setAccountNumber(accountNumberGeneratorService.generateAccountNumber());
        return bankAccountRepository.save(bankAccount);
    }

    @Override
    public List<BankAccount> findAll() {
        return bankAccountRepository.findAll();
    }

    @Override
    public BankAccount getByAccountNumber(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new BankApplicationNotFoundException("bank account does not exists. Account number: " + accountNumber));
    }
}
