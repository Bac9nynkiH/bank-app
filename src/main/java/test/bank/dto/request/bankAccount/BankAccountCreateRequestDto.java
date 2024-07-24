package test.bank.dto.request.bankAccount;


import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BankAccountCreateRequestDto {
    @DecimalMin(value = "0")
    private BigDecimal initialBalance;
}
