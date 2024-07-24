package test.bank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.bank.domain.banking.transaction.*;
import test.bank.exception.BankApplicationException;
import test.bank.exception.BankApplicationNegativeBalanceException;
import test.bank.exception.BankApplicationNotFoundException;
import test.bank.repository.BankAccountRepository;
import test.bank.repository.BankTransactionRepository;
import test.bank.service.interfaces.AccountTransactionsService;
import test.bank.util.TimeUtil;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountTransactionsServiceImpl implements AccountTransactionsService {
    private final BankTransactionRepository bankTransactionRepository;
    private final BankAccountRepository bankAccountRepository;


    @Override
    @Transactional
    public TransferTransaction transfer(String senderAccountNumber, String receiverAccountNumber, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BankApplicationException("amount should be positive");
        }
        if(senderAccountNumber.equals(receiverAccountNumber)){
            throw new BankApplicationException("senderAccountNumber and receiverAccountNumber can not be identical");
        }

        var timestamp = TimeUtil.currentTimeMillis();

        var sender = bankAccountRepository.getByAccountNumber(senderAccountNumber).orElseThrow(() -> new BankApplicationNotFoundException());
        var receiver = bankAccountRepository.getByAccountNumber(receiverAccountNumber).orElseThrow(() -> new BankApplicationNotFoundException());

        if(sender.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0)
            throw new BankApplicationNegativeBalanceException("sender does not have enough money: " + senderAccountNumber);

        var senderTransaction = new TransferTransaction();
        senderTransaction.setFlow(MoneyFlow.OUT);
        senderTransaction.setAmount(amount);
        senderTransaction.setTimestamp(timestamp);

        var receiverTransaction = new TransferTransaction();
        receiverTransaction.setFlow(MoneyFlow.IN);
        receiverTransaction.setAmount(amount);
        receiverTransaction.setTimestamp(timestamp);

        senderTransaction.setBankAccount(sender);
        receiverTransaction.setBankAccount(receiver);
        receiverTransaction.setVisavis(sender);
        senderTransaction.setVisavis(receiver);

        receiver.setBalance(receiver.getBalance().add(amount));
        sender.setBalance(sender.getBalance().subtract(amount));

        bankTransactionRepository.save(senderTransaction);
        bankTransactionRepository.save(receiverTransaction);

        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);

        return senderTransaction;
    }

    @Override
    @Transactional
    public WithdrawTransaction withdraw(String senderAccountNumber, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BankApplicationException("amount should be positive");
        }

        var timestamp = TimeUtil.currentTimeMillis();

        var sender = bankAccountRepository.getByAccountNumber(senderAccountNumber).orElseThrow(() -> new BankApplicationNotFoundException());

        if(sender.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new BankApplicationNegativeBalanceException("sender does not have enough money: " + senderAccountNumber);
        }

        var senderTransaction = new WithdrawTransaction();
        senderTransaction.setFlow(MoneyFlow.OUT);
        senderTransaction.setAmount(amount);
        senderTransaction.setTimestamp(timestamp);

        senderTransaction.setBankAccount(sender);

        sender.setBalance(sender.getBalance().subtract(amount));

        bankTransactionRepository.save(senderTransaction);

        bankAccountRepository.save(sender);

        return senderTransaction;
    }

    @Override
    @Transactional
    public DepositTransaction deposit(String receiverAccountNumber, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BankApplicationException("amount should be positive");
        }

        var timestamp = TimeUtil.currentTimeMillis();

        var receiver = bankAccountRepository.getByAccountNumber(receiverAccountNumber).orElseThrow(() -> new BankApplicationNotFoundException());

        var receiverTransaction = new DepositTransaction();
        receiverTransaction.setFlow(MoneyFlow.IN);
        receiverTransaction.setAmount(amount);
        receiverTransaction.setTimestamp(timestamp);

        receiverTransaction.setBankAccount(receiver);

        receiver.setBalance(receiver.getBalance().add(amount));

        bankTransactionRepository.save(receiverTransaction);

        bankAccountRepository.save(receiver);

        return receiverTransaction;
    }
}
