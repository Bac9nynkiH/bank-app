package test.bank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import test.bank.dto.request.transaction.TransactionRequestDto;
import test.bank.dto.request.transaction.TransferTransactionRequestDto;
import test.bank.dto.response.transaction.TransactionResponseDto;
import test.bank.dto.response.transaction.TransferTransactionResponseDto;
import test.bank.exception.BankApplicationBadRequestException;
import test.bank.service.interfaces.AccountTransactionsService;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class AccountTransactionController {
    private final AccountTransactionsService accountTransactionsService;
    @PostMapping("/transfer")
    public TransferTransactionResponseDto transfer(@RequestBody @Valid TransferTransactionRequestDto transactionRequestDto){
        if(transactionRequestDto.getSenderAccountNumber().equals(transactionRequestDto.getReceiverAccountNumber())){
            throw new BankApplicationBadRequestException("receiver and sender account number can not be the same");
        }
        return TransferTransactionResponseDto.of(accountTransactionsService.transfer(transactionRequestDto.getSenderAccountNumber(),transactionRequestDto.getReceiverAccountNumber(),transactionRequestDto.getAmount()));
    }
    @PostMapping("/withdraw")
    public TransactionResponseDto withdraw(@RequestBody @Valid TransactionRequestDto transactionRequestDto){
        return TransactionResponseDto.of(accountTransactionsService.withdraw(transactionRequestDto.getAccountNumber(),transactionRequestDto.getAmount()));
    }
    @PostMapping("/deposit")
    public TransactionResponseDto deposit(@RequestBody @Valid TransactionRequestDto transactionRequestDto){
        return TransactionResponseDto.of(accountTransactionsService.deposit(transactionRequestDto.getAccountNumber(),transactionRequestDto.getAmount()));
    }
}
