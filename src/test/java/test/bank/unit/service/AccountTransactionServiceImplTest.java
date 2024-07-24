package test.bank.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import test.bank.domain.banking.BankAccount;
import test.bank.exception.BankApplicationException;
import test.bank.repository.BankAccountRepository;
import test.bank.repository.BankTransactionRepository;
import test.bank.repository.DepositTransactionRepository;
import test.bank.repository.TransferTransactionRepository;
import test.bank.service.AccountTransactionsServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountTransactionServiceImplTest {
    @Mock
    private BankAccountRepository bankAccountRepository;
    @InjectMocks
    private AccountTransactionsServiceImpl accountTransactionsService;
    @Mock
    private DepositTransactionRepository depositTransactionRepository;
    @Mock
    private TransferTransactionRepository transferTransactionRepository;
    @Mock
    private BankTransactionRepository bankTransactionRepository;
    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";
    private final String BANK_ACCOUNT_NUMBER_SECOND = "0001110001110002";

    @Test
    public void depositSuccess() {
        var initialBalance = BigDecimal.ZERO;
        var bankAccount = new BankAccount(initialBalance, BANK_ACCOUNT_NUMBER);

        when(bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));

        accountTransactionsService.deposit(BANK_ACCOUNT_NUMBER, BigDecimal.ONE);

        assertEquals(initialBalance.add(BigDecimal.ONE), bankAccount.getBalance());
        verify(bankAccountRepository, times(1)).save(any());
        verify(bankTransactionRepository, times(1)).save(any());
        verifyNoMoreInteractions(bankTransactionRepository);
        verifyNoMoreInteractions(bankAccountRepository);
    }

    @Test
    public void withdrawSuccess() {
        var initialBalance = BigDecimal.ONE;
        var bankAccount = new BankAccount(initialBalance, BANK_ACCOUNT_NUMBER);

        when(bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));

        accountTransactionsService.withdraw(BANK_ACCOUNT_NUMBER, BigDecimal.ONE);

        assertEquals(initialBalance.subtract(BigDecimal.ONE), bankAccount.getBalance());
        verify(bankAccountRepository, times(1)).save(any());
        verify(bankTransactionRepository, times(1)).save(any());
        verifyNoMoreInteractions(bankTransactionRepository);
        verifyNoMoreInteractions(bankAccountRepository);
    }

    @Test
    public void transferSuccess() {
        var initialBalance = BigDecimal.ONE;
        var bankAccount = new BankAccount(initialBalance, BANK_ACCOUNT_NUMBER);

        var initialBalance2 = BigDecimal.ZERO;
        var bankAccount2 = new BankAccount(initialBalance2, BANK_ACCOUNT_NUMBER_SECOND);

        when(bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER_SECOND)).thenReturn(Optional.of(bankAccount2));

        accountTransactionsService.transfer(BANK_ACCOUNT_NUMBER, BANK_ACCOUNT_NUMBER_SECOND, BigDecimal.ONE);
        assertEquals(initialBalance.subtract(BigDecimal.ONE), bankAccount.getBalance());
        assertEquals(initialBalance2.add(BigDecimal.ONE), bankAccount2.getBalance());
        verify(bankAccountRepository, times(2)).save(any());
        verify(bankTransactionRepository, times(2)).save(any());
        verifyNoMoreInteractions(bankTransactionRepository);
        verifyNoMoreInteractions(bankAccountRepository);
    }

    @Test
    public void depositNegative() {
        assertThrowsExactly(BankApplicationException.class, () -> {
                    accountTransactionsService.deposit(BANK_ACCOUNT_NUMBER, new BigDecimal(-1));
                }
        );
        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(bankTransactionRepository);
    }

    @Test
    public void withdrawNegative() {
        assertThrowsExactly(BankApplicationException.class, () -> {
                    accountTransactionsService.withdraw(BANK_ACCOUNT_NUMBER, new BigDecimal(-1));
                }
        );
        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(bankTransactionRepository);
    }

    @Test
    public void transferNegative() {
        assertThrowsExactly(BankApplicationException.class, () -> {
                    accountTransactionsService.transfer(BANK_ACCOUNT_NUMBER, BANK_ACCOUNT_NUMBER_SECOND, new BigDecimal(-1));
                }
        );
        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(bankTransactionRepository);
    }

    @Test
    public void transferSamePerson() {
        assertThrowsExactly(BankApplicationException.class, () -> {
                    accountTransactionsService.transfer(BANK_ACCOUNT_NUMBER, BANK_ACCOUNT_NUMBER, new BigDecimal(1));
                }
        );
        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(bankTransactionRepository);
    }

}