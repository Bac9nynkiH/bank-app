package test.bank.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import test.bank.domain.banking.BankAccount;
import test.bank.domain.banking.transaction.DepositTransaction;
import test.bank.exception.BankApplicationException;
import test.bank.exception.BankApplicationNegativeBalanceException;
import test.bank.exception.BankApplicationNotFoundException;
import test.bank.repository.BankAccountRepository;
import test.bank.service.AccountManagementServiceImpl;
import test.bank.service.interfaces.AccountNumberGeneratorService;
import test.bank.service.interfaces.AccountTransactionsService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountManagementServiceImplTest {
    @Mock
    private BankAccountRepository bankAccountRepository;
    @Spy
    private AccountNumberGeneratorService accountNumberGeneratorService;
    @Mock
    private AccountTransactionsService accountTransactionsService;
    @InjectMocks
    private AccountManagementServiceImpl service;
    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";

    @ParameterizedTest
    @ValueSource(strings = {"0"})
    void createBankAccountSuccessZeroDeposit(String initialBalanceStr) {
        var expected = new BankAccount(UUID.randomUUID(), BigDecimal.ZERO, BANK_ACCOUNT_NUMBER);
        when(bankAccountRepository.save(any())).thenReturn(expected);
        when(accountNumberGeneratorService.generateAccountNumber()).thenReturn(BANK_ACCOUNT_NUMBER);

        var returned = service.createBankAccount(new BigDecimal(initialBalanceStr));
        assertEquals(expected, returned);
        assertEquals(expected.getBalance(), returned.getBalance());

        verify(bankAccountRepository, times(1)).save(any());
        verifyNoMoreInteractions(bankAccountRepository);
        verifyNoMoreInteractions(accountTransactionsService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "10"})
    void createBankAccountSuccess(String initialBalanceStr) {
        var balance = new BigDecimal(initialBalanceStr);
        var expected = new BankAccount(UUID.randomUUID(), BigDecimal.ZERO, BANK_ACCOUNT_NUMBER);
        when(bankAccountRepository.save(any())).thenReturn(expected);
        when(accountNumberGeneratorService.generateAccountNumber()).thenReturn(BANK_ACCOUNT_NUMBER);
        when(accountTransactionsService.deposit(BANK_ACCOUNT_NUMBER, balance)).then(
                (Answer) invocation -> {
                    expected.setBalance(balance);
                    return new DepositTransaction();
                });

        var returned = service.createBankAccount(new BigDecimal(initialBalanceStr));
        assertEquals(expected, returned);
        assertEquals(expected.getBalance(), returned.getBalance());

        verify(bankAccountRepository, times(1)).save(any());
        verify(accountTransactionsService, times(1)).deposit(BANK_ACCOUNT_NUMBER, balance);
        verifyNoMoreInteractions(bankAccountRepository);
        verifyNoMoreInteractions(accountTransactionsService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1"})
    void createBankAccountNegativeBalance(String initialBalanceStr) {
        BigDecimal initialBalance = new BigDecimal(initialBalanceStr);

        assertThrowsExactly(BankApplicationNegativeBalanceException.class, () -> service.createBankAccount(initialBalance));
        assertThrows(BankApplicationException.class, () -> service.createBankAccount(initialBalance));

        verify(bankAccountRepository, times(0)).save(any());
        verify(accountTransactionsService, times(0)).deposit(anyString(), any());
    }

    @Test
    void findAll() {
        List<BankAccount> expected = List.of(new BankAccount(UUID.randomUUID(), BigDecimal.ZERO, BANK_ACCOUNT_NUMBER));
        when(bankAccountRepository.findAll()).thenReturn(expected);

        var returned = service.findAll();

        verify(bankAccountRepository, times(1)).findAll();
        verifyNoMoreInteractions(bankAccountRepository);

        assertArrayEquals(expected.toArray(), returned.toArray());
    }

    @Test
    void getByAccountNumberNotFound() {
        when(bankAccountRepository.findByAccountNumber(BANK_ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        assertThrows(BankApplicationNotFoundException.class, () -> service.getByAccountNumber(BANK_ACCOUNT_NUMBER));
        verify(bankAccountRepository, times(1)).findByAccountNumber(BANK_ACCOUNT_NUMBER);
        verifyNoMoreInteractions(bankAccountRepository);

    }

    @Test
    void getByAccountNumberSuccess() {
        var expected = new BankAccount(UUID.randomUUID(), BigDecimal.ZERO, BANK_ACCOUNT_NUMBER);
        when(bankAccountRepository.findByAccountNumber(BANK_ACCOUNT_NUMBER)).thenReturn(Optional.of(expected));

        var returned = service.getByAccountNumber(BANK_ACCOUNT_NUMBER);
        assertEquals(expected, returned);
        assertEquals(expected.getBalance(), returned.getBalance());
        verify(bankAccountRepository, times(1)).findByAccountNumber(BANK_ACCOUNT_NUMBER);
        verifyNoMoreInteractions(bankAccountRepository);
    }
}