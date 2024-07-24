package test.bank.domain.banking.transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.*;
import test.bank.domain.banking.BankAccount;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(name = "withdraw_transaction")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class WithdrawTransaction extends BankTransaction{
    public WithdrawTransaction(UUID id, BigDecimal amount, Long timestamp, BankAccount bankAccount, MoneyFlow flow) {
        super(id, amount, timestamp, bankAccount, flow);
    }
}
