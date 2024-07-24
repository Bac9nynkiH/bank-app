package test.bank.integration.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import test.bank.domain.banking.BankAccount;
import test.bank.repository.BankAccountRepository;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
public class BankAccountRepositoryTest {
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";

    @BeforeEach
    public void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        bankAccountRepository.save(new BankAccount(BigDecimal.ZERO, BANK_ACCOUNT_NUMBER));
    }

    @AfterEach
    public void cleanUp() {
        bankAccountRepository.deleteAll();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void testLockBelowTimeout() throws InterruptedException {
        int numberOfThreads = 1;
        var semaphore = new Semaphore(0);
        var permitThreadStart = new Semaphore(0);
        var executor = Executors.newFixedThreadPool(numberOfThreads);
        var threadsDone = new CountDownLatch(numberOfThreads);

        //additional threads initiate transaction
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    transactionTemplate.execute(new TransactionCallback() {
                        public Object doInTransaction(TransactionStatus status) {
                            bankAccountRepository.getByAccountNumber(BANK_ACCOUNT_NUMBER);
                            semaphore.release();
                            try {
                                permitThreadStart.acquire();
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            status.flush();
                            return null;
                        }
                    });
                } finally {
                    threadsDone.countDown();
                }
            });
        }


        //main thread initiate transaction
        try {
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
        } catch (PessimisticLockingFailureException e) {
            System.out.println("[PessimisticLockingFailureException] during lock test");
            fail();
        }
        executor.awaitTermination(10, TimeUnit.SECONDS);
        threadsDone.await();
    }
}
