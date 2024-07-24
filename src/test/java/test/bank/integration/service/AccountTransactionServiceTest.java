package test.bank.integration.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import test.bank.domain.banking.BankAccount;
import test.bank.domain.banking.transaction.DepositTransaction;
import test.bank.domain.banking.transaction.MoneyFlow;
import test.bank.exception.BankApplicationException;
import test.bank.exception.BankApplicationNegativeBalanceException;
import test.bank.repository.BankAccountRepository;
import test.bank.repository.BankTransactionRepository;
import test.bank.repository.DepositTransactionRepository;
import test.bank.repository.TransferTransactionRepository;
import test.bank.service.interfaces.AccountTransactionsService;

import java.math.BigDecimal;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@SpringBootTest
public class AccountTransactionServiceTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private AccountTransactionsService accountTransactionsService;
    @Autowired
    private BankTransactionRepository bankTransactionRepository;
    @Autowired
    private DepositTransactionRepository depositTransactionRepository;
    @Autowired
    private TransferTransactionRepository transferTransactionRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";
    private final String BANK_ACCOUNT_NUMBER_SECOND = "0001110001110002";

    @AfterEach
    public void cleanUp(){
        bankTransactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
    }
    @BeforeEach
    public void setUp(){
        transactionTemplate = new TransactionTemplate(transactionManager);

        var bankAccount1 = bankAccountRepository.save(new BankAccount(BigDecimal.TEN,BANK_ACCOUNT_NUMBER));
        depositTransactionRepository.save(new DepositTransaction(BigDecimal.TEN,System.currentTimeMillis(),bankAccount1, MoneyFlow.IN));

        var bankAccount2 = bankAccountRepository.save(new BankAccount(BigDecimal.TEN,BANK_ACCOUNT_NUMBER_SECOND));
        depositTransactionRepository.save(new DepositTransaction(BigDecimal.TEN,System.currentTimeMillis(),bankAccount2, MoneyFlow.IN));
    }

    @Test
    @Transactional
    @Commit
    public void depositSuccess(){
        var initialBalance = bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER_SECOND).get().getBalance();
        var initialMoneyInCount = depositTransactionRepository.findAll().size();

        accountTransactionsService.deposit(BANK_ACCOUNT_NUMBER,BigDecimal.ONE);

        assertEquals(initialBalance.add(BigDecimal.ONE),bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER).get().getBalance());
        assertEquals(initialMoneyInCount + 1,depositTransactionRepository.findAll().size());
    }

    @Test
    @Transactional
    @Commit
    public void withdrawSuccess(){
        var withdraws = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER_SECOND);
        var initialBalance = bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER_SECOND).get().getBalance();
        var initialMoneyOutCount = withdraws.stream().filter(t -> t.getFlow().equals(MoneyFlow.OUT)).count();

        accountTransactionsService.withdraw(BANK_ACCOUNT_NUMBER,BigDecimal.ONE);

        var withdrawsRes = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER);

        assertEquals(initialBalance.subtract(BigDecimal.ONE),bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER).get().getBalance());
        assertEquals(initialMoneyOutCount + 1,withdrawsRes.stream().filter(t -> t.getFlow().equals(MoneyFlow.OUT)).count());
    }

    @Test
    @Transactional
    @Commit
    public void transferSuccess(){
        assertEquals(0,transferTransactionRepository.findAll().size());

        var withdraws = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER_SECOND);
        var initialBalance = bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER_SECOND).get().getBalance();
        var initialMoneyOutCount = withdraws.stream().filter(t -> t.getFlow().equals(MoneyFlow.OUT)).count();

        var withdraws2 = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER_SECOND);
        var initialBalance2 = bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER_SECOND).get().getBalance();
        var initialMoneyInCount = withdraws2.stream().filter(t -> t.getFlow().equals(MoneyFlow.IN)).count();

        accountTransactionsService.transfer(BANK_ACCOUNT_NUMBER,BANK_ACCOUNT_NUMBER_SECOND,BigDecimal.ONE);

        var withdrawsRes = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER);
        assertEquals(initialBalance.subtract(BigDecimal.ONE),bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER).get().getBalance());
        assertEquals(initialMoneyOutCount + 1,withdrawsRes.stream().filter(t -> t.getFlow().equals(MoneyFlow.OUT)).count());

        var withdraws2Res = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER_SECOND);
        assertEquals(initialBalance2.add(BigDecimal.ONE),bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER_SECOND).get().getBalance());
        assertEquals(initialMoneyInCount + 1,withdraws2Res.stream().filter(t -> t.getFlow().equals(MoneyFlow.IN)).count());

        var transfers = transferTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER);
        var transfers2 = transferTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER_SECOND);

        assertEquals(1,transfers.size());
        assertEquals(1,transfers2.size());

        assertEquals(BANK_ACCOUNT_NUMBER_SECOND,transfers.get(0).getVisavis().getAccountNumber());
        assertEquals(BANK_ACCOUNT_NUMBER,transfers2.get(0).getVisavis().getAccountNumber());
    }
    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Commit
    public void transferNegativeBalance(){
        final BigDecimal[] initialBalance = new BigDecimal[1];
        final BigDecimal[] initialBalance2 = new BigDecimal[1];
        final long[] initialMoneyOutCount = new long[1];
        final long[] initialMoneyInCount = new long[1];
        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                initialBalance[0] = bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER).get().getBalance();
                initialBalance2[0] = bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER_SECOND).get().getBalance();

                var withdraws = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER);
                initialMoneyOutCount[0] = withdraws.stream().filter(t -> t.getFlow().equals(MoneyFlow.OUT)).count();

                var withdraws2 = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER_SECOND);
                initialMoneyInCount[0] = withdraws2.stream().filter(t -> t.getFlow().equals(MoneyFlow.IN)).count();
                return null;
            }
        });

        assertThrowsExactly(BankApplicationNegativeBalanceException.class,() -> {
            accountTransactionsService.transfer(BANK_ACCOUNT_NUMBER,BANK_ACCOUNT_NUMBER_SECOND,BigDecimal.valueOf(11));
        });
        assertThrows(BankApplicationException.class,() -> {
            accountTransactionsService.transfer(BANK_ACCOUNT_NUMBER,BANK_ACCOUNT_NUMBER_SECOND,BigDecimal.valueOf(11));
        });

        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                var withdraws = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER);
                var withdraws2 = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER_SECOND);

                assertEquals(initialBalance[0], bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER).get().getBalance());
                assertEquals(initialMoneyOutCount[0], withdraws.stream().filter(t -> t.getFlow().equals(MoneyFlow.OUT)).count());

                assertEquals(initialBalance2[0], bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER_SECOND).get().getBalance());
                assertEquals(initialMoneyInCount[0], withdraws2.stream().filter(t -> t.getFlow().equals(MoneyFlow.IN)).count());

                return null;
            }
        });

    }

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Commit
    public void withdrawNegativeBalance(){
        final BigDecimal[] initialBalance = new BigDecimal[1];
        final long[] initialMoneyOutCount = new long[1];
        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                initialBalance[0] = bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER).get().getBalance();

                var withdraws = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER);
                initialMoneyOutCount[0] = withdraws.stream().filter(t -> t.getFlow().equals(MoneyFlow.OUT)).count();

                return null;
            }
        });

        assertThrowsExactly(BankApplicationNegativeBalanceException.class,() -> {
            accountTransactionsService.withdraw(BANK_ACCOUNT_NUMBER,BigDecimal.valueOf(11));
        });
        assertThrows(BankApplicationException.class,() -> {
            accountTransactionsService.withdraw(BANK_ACCOUNT_NUMBER,BigDecimal.valueOf(11));
        });

        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                var withdraws = bankTransactionRepository.findAllByBankAccountAccountNumber(BANK_ACCOUNT_NUMBER);
                assertEquals(initialBalance[0],bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER).get().getBalance());
                assertEquals(initialMoneyOutCount[0],withdraws.stream().filter(t -> t.getFlow().equals(MoneyFlow.OUT)).count());
                return null;
            }
        });
    }


    @Test
    public void testWithdrawLock() throws InterruptedException {
        int numberOfThreads = 1;
        Semaphore semaphore = new Semaphore(0);
        Semaphore permitThreadStart = new Semaphore(0);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch threadsDone = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    executeThreadForLockTest(9000, () -> accountTransactionsService.withdraw(BANK_ACCOUNT_NUMBER, BigDecimal.ONE), semaphore, permitThreadStart);
                }
                finally {
                    threadsDone.countDown();
                }
            });
        }
        try {
            executeMainThreadForLockTest(semaphore,permitThreadStart);
            fail();
        }
        catch (PessimisticLockingFailureException e){
            System.out.println("[PessimisticLockingFailureException] during lock test");
            assertTrue(true);
        }
        executor.awaitTermination(30,TimeUnit.SECONDS);
        threadsDone.await();
    }

    @Test
    public void testDepositLock() throws InterruptedException {
        int numberOfThreads = 1;
        Semaphore semaphore = new Semaphore(0);
        Semaphore permitThreadStart = new Semaphore(0);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch threadsDone = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    executeThreadForLockTest(9000, () -> accountTransactionsService.deposit(BANK_ACCOUNT_NUMBER, BigDecimal.ONE), semaphore, permitThreadStart);
                }
                finally {
                    threadsDone.countDown();
                }
            });
        }
        try {
            executeMainThreadForLockTest(semaphore,permitThreadStart);
            fail();
        }
        catch (PessimisticLockingFailureException e){
            System.out.println("[PessimisticLockingFailureException] during lock test");
            assertTrue(true);
        }
        executor.awaitTermination(30,TimeUnit.SECONDS);
        threadsDone.await();
    }

    @Test
    public void testTransferLock() throws InterruptedException {
        int numberOfThreads = 1;
        Semaphore semaphore = new Semaphore(0);
        Semaphore permitThreadStart = new Semaphore(0);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch threadsDone = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    executeThreadForLockTest(9000, () -> accountTransactionsService.transfer(BANK_ACCOUNT_NUMBER,BANK_ACCOUNT_NUMBER_SECOND, BigDecimal.ONE), semaphore, permitThreadStart);
                }
                finally {
                    threadsDone.countDown();
                }
            });
        }
        try {
            executeMainThreadForLockTest(semaphore,permitThreadStart);
            fail();
        }
        catch (PessimisticLockingFailureException e){
            System.out.println("[PessimisticLockingFailureException] during lock test");
            assertTrue(true);
        }
        executor.awaitTermination(30,TimeUnit.SECONDS);
        threadsDone.await();
    }

    private void executeMainThreadForLockTest(Semaphore semaphore,Semaphore permitThreadStart){
        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                permitThreadStart.release();
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER);
                status.flush();
                return null;
            }
        });
    }
    private void executeThreadForLockTest(long sleepDuration, ExecutableMethod method, Semaphore semaphore,Semaphore permitThreadStart){
        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                method.execute();
                semaphore.release();
                try {
                    permitThreadStart.acquire();
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                status.flush();
                return null;
            }
        });
    }

    private interface ExecutableMethod{
        void execute();
    }
}