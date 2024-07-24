package test.bank.domain.banking.transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.bank.domain.banking.BankAccount;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(name = "transfer_transaction")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class TransferTransaction extends BankTransaction {
    public TransferTransaction(UUID id, BigDecimal amount, Long timestamp, BankAccount bankAccount, MoneyFlow flow, BankAccount visavis) {
        super(id, amount, timestamp, bankAccount, flow);
        this.visavis = visavis;
    }

    @OneToOne
    private BankAccount visavis;
}
