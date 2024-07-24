package test.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.bank.domain.banking.transaction.WithdrawTransaction;

import java.util.UUID;

@Repository
public interface WithdrawTransactionRepository extends JpaRepository<WithdrawTransaction, UUID> {
}
