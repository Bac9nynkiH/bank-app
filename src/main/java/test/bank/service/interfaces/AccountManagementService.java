package test.bank.service.interfaces;

import test.bank.domain.banking.BankAccount;

import java.math.BigDecimal;
import java.util.List;

public interface AccountManagementService {
    BankAccount createBankAccount(BigDecimal initialBalance);
    List<BankAccount> findAll();
    BankAccount getByAccountNumber(String accountNumber);
}
