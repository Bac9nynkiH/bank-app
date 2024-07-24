package test.bank.domain.banking;

import jakarta.persistence.*;
import lombok.*;
import test.bank.domain.banking.transaction.BankTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "bank_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id","accountNumber"})
@ToString
public class BankAccount {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    @Column(name = "account_number",unique = true)
    private String accountNumber;
    @OneToMany(mappedBy = "bankAccount")
    private List<BankTransaction> transactions = new ArrayList<>();

    public BankAccount(BigDecimal balance, String accountNumber) {
        this.balance = balance;
        this.accountNumber = accountNumber;
    }
    public BankAccount(UUID id, BigDecimal balance, String accountNumber) {
        this.id = id;
        this.balance = balance;
        this.accountNumber = accountNumber;
    }
}
