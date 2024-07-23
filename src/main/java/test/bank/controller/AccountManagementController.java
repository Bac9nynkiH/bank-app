package test.bank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import test.bank.domain.banking.BankAccount;
import test.bank.dto.bankAccount.BankAccountCreateDto;
import test.bank.dto.bankAccount.BankAccountResponseDto;
import test.bank.service.interfaces.AccountManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
public class AccountManagementController {
    private final AccountManagementService accountManagementService;
    @PostMapping("/create")
    public BankAccountResponseDto createBankAccount(@RequestBody @Valid BankAccountCreateDto bankAccountCreateDto){
        return BankAccountResponseDto.of(accountManagementService.createBankAccount(bankAccountCreateDto.getInitialBalance()));
    }
    @GetMapping("/all")
    public List<BankAccountResponseDto> findAll(){
        return accountManagementService.findAll().stream().map(
                BankAccountResponseDto::of
        ).toList();
    }
    @GetMapping("/{accountNumber}")
    public BankAccountResponseDto getByAccountNumber(@PathVariable(name = "accountNumber") String accountNumber){
        return BankAccountResponseDto.of(accountManagementService.getByAccountNumber(accountNumber));
    }
}