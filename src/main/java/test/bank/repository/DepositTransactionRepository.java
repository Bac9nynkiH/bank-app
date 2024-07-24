package test.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.bank.domain.banking.transaction.DepositTransaction;

import java.util.UUID;

@Repository
public interface DepositTransactionRepository extends JpaRepository<DepositTransaction, UUID> {
}
