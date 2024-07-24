package test.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.bank.domain.banking.transaction.BankTransaction;

import java.util.List;
import java.util.UUID;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID> {
    List<BankTransaction> findAllByBankAccountAccountNumber(String bankAccountNumber);

}
