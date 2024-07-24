package test.bank.dto.response.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.bank.domain.banking.transaction.BankTransaction;
import test.bank.domain.banking.transaction.MoneyFlow;
import test.bank.domain.banking.transaction.TransferTransaction;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {
    private Long timestamp;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal amount;
    private String bankAccountNumber;
    private MoneyFlow flow;

    public static TransactionResponseDto of(BankTransaction transaction){
        return new TransactionResponseDto(transaction.getTimestamp(),transaction.getAmount(),transaction.getBankAccount().getAccountNumber(),transaction.getFlow());
    }
}
