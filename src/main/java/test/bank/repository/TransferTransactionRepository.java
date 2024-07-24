package test.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.bank.domain.banking.transaction.TransferTransaction;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, UUID> {
    List<TransferTransaction> findAllByBankAccountAccountNumber(String accountNumber);
}
