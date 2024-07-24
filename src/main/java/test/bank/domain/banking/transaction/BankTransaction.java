package test.bank.domain.banking.transaction;

import jakarta.persistence.*;
import lombok.*;
import test.bank.domain.banking.BankAccount;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(name = "bank_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Inheritance(strategy = InheritanceType.JOINED)
public class BankTransaction {
    public BankTransaction(BigDecimal amount, Long timestamp, BankAccount bankAccount, MoneyFlow flow) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.bankAccount = bankAccount;
        this.flow = flow;
    }

    @Id
    @GeneratedValue
    private UUID id;
    private BigDecimal amount;
    private Long timestamp;
    @ManyToOne
    private BankAccount bankAccount;
    @Enumerated(EnumType.STRING)
    private MoneyFlow flow;
}
