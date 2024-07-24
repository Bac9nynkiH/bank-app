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
public class TransactionRequestDto {
    @Pattern(regexp = "\\d{16}", message = "must be exactly 16 digits")
    private String accountNumber;
    @DecimalMin(value = "0",inclusive = false)
    private BigDecimal amount;
}
