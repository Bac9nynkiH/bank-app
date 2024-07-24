package test.bank.dto.response.transaction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.bank.domain.banking.transaction.MoneyFlow;
import test.bank.domain.banking.transaction.TransferTransaction;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransferTransactionResponseDto extends TransactionResponseDto {
    private String visavisAccountNumber;

    public TransferTransactionResponseDto(Long timestamp, BigDecimal amount, String bankAccountNumber, MoneyFlow flow, String visavisAccountNumber) {
        super(timestamp, amount, bankAccountNumber, flow);
        this.visavisAccountNumber = visavisAccountNumber;
    }

    public static TransferTransactionResponseDto of(TransferTransaction transaction) {
        return new TransferTransactionResponseDto(
                transaction.getTimestamp(),
                transaction.getAmount(),
                transaction.getBankAccount().getAccountNumber(),
                transaction.getFlow(),
                transaction.getVisavis().getAccountNumber()
        );
    }
}
