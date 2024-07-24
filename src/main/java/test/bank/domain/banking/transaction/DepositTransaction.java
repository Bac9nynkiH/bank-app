package test.bank.domain.banking.transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.bank.domain.banking.BankAccount;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(name = "deposit_transaction")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class DepositTransaction extends BankTransaction{
    public DepositTransaction(BigDecimal amount, Long timestamp, BankAccount bankAccount, MoneyFlow flow) {
        super(amount, timestamp, bankAccount, flow);
    }

    public DepositTransaction(UUID id, BigDecimal amount, Long timestamp, BankAccount bankAccount, MoneyFlow flow) {
        super(id, amount, timestamp, bankAccount, flow);
    }
}