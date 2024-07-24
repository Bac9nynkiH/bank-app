package test.bank.dto.response.bankAccount;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal balance;
    private String accountNumber;

    public BankAccountResponseDto(UUID id, BigDecimal balance, String accountNumber) {
        this.id = id;
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
        this.accountNumber = accountNumber;
    }

    public static BankAccountResponseDto of(BankAccount bankAccount) {
        return new BankAccountResponseDto(bankAccount.getId(), bankAccount.getBalance(), bankAccount.getAccountNumber());
    }
}
