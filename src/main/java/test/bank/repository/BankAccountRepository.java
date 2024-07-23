package test.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.bank.domain.banking.BankAccount;

import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
}
