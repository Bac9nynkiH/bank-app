package test.bank.dto.bankAccount;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.bank.domain.banking.BankAccount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BankAccountResponseDto {
    private UUID id;
    private BigDecimal balance;
    private String accountNumber;

    public BankAccountResponseDto(UUID id, BigDecimal balance, String accountNumber) {
        balance = balance.setScale(2, RoundingMode.HALF_UP);

        this.id = id;
        this.balance = balance;
        this.accountNumber = accountNumber;
    }

    public static BankAccountResponseDto of(BankAccount bankAccount) {
        return new BankAccountResponseDto(bankAccount.getId(),bankAccount.getBalance(),bankAccount.getAccountNumber());
    }
}
