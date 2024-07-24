package test.bank.dto.request.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferTransactionRequestDto {
    @Pattern(regexp = "\\d{16}", message = "must be exactly 16 digits")
    private String senderAccountNumber;
    @Pattern(regexp = "\\d{16}", message = "must be exactly 16 digits")
    private String receiverAccountNumber;
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal amount;
}
