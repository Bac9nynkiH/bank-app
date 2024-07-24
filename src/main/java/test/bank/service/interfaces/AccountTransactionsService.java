package test.bank.service.interfaces;

import test.bank.domain.banking.BankAccount;
import test.bank.domain.banking.transaction.BankTransaction;
import test.bank.domain.banking.transaction.DepositTransaction;
import test.bank.domain.banking.transaction.TransferTransaction;
import test.bank.domain.banking.transaction.WithdrawTransaction;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountTransactionsService {
    TransferTransaction transfer(String senderAccountNumber, String receiverAccountNumber, BigDecimal amount);
    WithdrawTransaction withdraw(String senderAccountNumber, BigDecimal amount);
    DepositTransaction deposit(String receiverAccountNumber, BigDecimal amount);
}
