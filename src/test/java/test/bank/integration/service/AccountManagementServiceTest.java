package test.bank.integration.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import test.bank.domain.banking.BankAccount;
import test.bank.domain.banking.transaction.BankTransaction;
import test.bank.domain.banking.transaction.DepositTransaction;
import test.bank.domain.banking.transaction.MoneyFlow;
import test.bank.exception.BankApplicationException;
import test.bank.exception.BankApplicationNegativeBalanceException;
import test.bank.exception.BankApplicationNotFoundException;
import test.bank.repository.BankAccountRepository;
import test.bank.repository.BankTransactionRepository;
import test.bank.repository.DepositTransactionRepository;
import test.bank.service.AccountManagementServiceImpl;
import test.bank.service.interfaces.AccountNumberGeneratorService;
import test.bank.service.interfaces.AccountTransactionsService;
import test.bank.util.TimeUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class AccountManagementServiceTest {
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @MockBean
    private AccountNumberGeneratorService accountNumberGeneratorService;
    @Autowired
    private AccountTransactionsService accountTransactionsService;
    @Autowired
    private AccountManagementServiceImpl service;
    @Autowired
    private BankTransactionRepository bankTransactionRepository;
    @Autowired
    private DepositTransactionRepository depositTransactionRepository;
    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";


    @AfterEach
    public void cleanUp(){
        bankTransactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1","10"})
    @Transactional
    @Commit
    void createBankAccountSuccess(String initialBalanceStr) {
        when(accountNumberGeneratorService.generateAccountNumber()).thenReturn(BANK_ACCOUNT_NUMBER);

        var expected = new BankAccount(new BigDecimal(initialBalanceStr),BANK_ACCOUNT_NUMBER);

        var returned = service.createBankAccount(new BigDecimal(initialBalanceStr));
        var resultTransactions = depositTransactionRepository.findAll();

        assertEquals(expected.getAccountNumber(),returned.getAccountNumber());
        assertEquals(expected.getBalance(),returned.getBalance());
        assertEquals(1,resultTransactions.size());
        assertEquals(BANK_ACCOUNT_NUMBER,resultTransactions.get(0).getBankAccount().getAccountNumber());
        assertEquals(MoneyFlow.IN,resultTransactions.get(0).getFlow());
        assertEquals(new BigDecimal(initialBalanceStr),resultTransactions.get(0).getAmount());
    }
    @ParameterizedTest
    @ValueSource(strings = {"-1"})
    void createBankAccountNegativeBalance(String initialBalanceStr) {
        when(accountNumberGeneratorService.generateAccountNumber()).thenReturn(BANK_ACCOUNT_NUMBER);

        BigDecimal initialBalance = new BigDecimal(initialBalanceStr);

        assertThrowsExactly(BankApplicationNegativeBalanceException.class,() -> service.createBankAccount(initialBalance));
        assertThrows(BankApplicationException.class,() -> service.createBankAccount(initialBalance));
        assertFalse(bankAccountRepository.findByAccountNumber(BANK_ACCOUNT_NUMBER).isPresent());
    }

    @Test
    @Transactional
    void findAll() {
        var expected = new BankAccount(UUID.randomUUID(),BigDecimal.ZERO,BANK_ACCOUNT_NUMBER);
        expected = bankAccountRepository.save(expected);

        var returned = service.findAll();

        assertArrayEquals(List.of(expected).toArray(),returned.toArray());
    }

    @Test
    void getByAccountNumberNotFound() {
        assertThrows(BankApplicationNotFoundException.class,() -> service.getByAccountNumber(BANK_ACCOUNT_NUMBER));

    }
    @Test
    void getByAccountNumberSuccess() {
        var expected = bankAccountRepository.save(new BankAccount(UUID.randomUUID(),BigDecimal.ZERO,BANK_ACCOUNT_NUMBER));

        var returned = service.getByAccountNumber(BANK_ACCOUNT_NUMBER);
        assertEquals(expected,returned);
        assertEquals(expected.getBalance().setScale(2,RoundingMode.HALF_UP),returned.getBalance());
    }
}