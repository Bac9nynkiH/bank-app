package test.bank.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import test.bank.controller.AccountManagementController;
import test.bank.controller.AccountTransactionController;
import test.bank.domain.banking.BankAccount;
import test.bank.domain.banking.transaction.DepositTransaction;
import test.bank.domain.banking.transaction.MoneyFlow;
import test.bank.domain.banking.transaction.TransferTransaction;
import test.bank.domain.banking.transaction.WithdrawTransaction;
import test.bank.dto.request.bankAccount.BankAccountCreateRequestDto;
import test.bank.dto.request.transaction.TransactionRequestDto;
import test.bank.dto.request.transaction.TransferTransactionRequestDto;
import test.bank.dto.response.bankAccount.BankAccountResponseDto;
import test.bank.dto.response.transaction.TransactionResponseDto;
import test.bank.dto.response.transaction.TransferTransactionResponseDto;
import test.bank.service.interfaces.AccountManagementService;
import test.bank.service.interfaces.AccountTransactionsService;
import test.bank.util.TimeUtil;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountTransactionController.class)
public class AccountTransactionControllerTest {
    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";
    private final String BANK_ACCOUNT_NUMBER_SECOND = "0001110001110002";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountTransactionsService accountTransactionsService;

    @Autowired
    private ObjectMapper objectMapper;
    @Test
    void transferSuccess() throws Exception {
        var timestamp = TimeUtil.currentTimeMillis();
        var dto = new TransferTransactionRequestDto(BANK_ACCOUNT_NUMBER,BANK_ACCOUNT_NUMBER_SECOND,BigDecimal.ONE);

        var expected = new TransferTransactionResponseDto(timestamp,BigDecimal.ONE,BANK_ACCOUNT_NUMBER, MoneyFlow.OUT,BANK_ACCOUNT_NUMBER_SECOND);
        var entity = new TransferTransaction(UUID.randomUUID(),BigDecimal.ONE,timestamp,new BankAccount(UUID.randomUUID(),BigDecimal.TEN,BANK_ACCOUNT_NUMBER),MoneyFlow.OUT,new BankAccount(UUID.randomUUID(),BigDecimal.TEN,BANK_ACCOUNT_NUMBER_SECOND));
        when(accountTransactionsService.transfer(dto.getSenderAccountNumber(),dto.getReceiverAccountNumber(),dto.getAmount())).thenReturn(entity);

        var resp = mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        var dtoResp = objectMapper.readValue(resp.getContentAsString(),TransferTransactionResponseDto.class);

        assertEquals(expected, dtoResp);
        verify(accountTransactionsService,times(1)).transfer(dto.getSenderAccountNumber(),dto.getReceiverAccountNumber(),dto.getAmount());
        verifyNoMoreInteractions(accountTransactionsService);
    }
    @Test
    void withdrawSuccess() throws Exception {
        var timestamp = TimeUtil.currentTimeMillis();
        var dto = new TransactionRequestDto(BANK_ACCOUNT_NUMBER,BigDecimal.ONE);

        var expected = new TransactionResponseDto(timestamp,BigDecimal.ONE,BANK_ACCOUNT_NUMBER, MoneyFlow.OUT);
        var entity = new WithdrawTransaction(UUID.randomUUID(),BigDecimal.ONE,timestamp,new BankAccount(UUID.randomUUID(),BigDecimal.TEN,BANK_ACCOUNT_NUMBER),MoneyFlow.OUT);
        when(accountTransactionsService.withdraw(dto.getAccountNumber(),dto.getAmount())).thenReturn(entity);

        var resp = mockMvc.perform(post("/api/transaction/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        var dtoResp = objectMapper.readValue(resp.getContentAsString(),TransferTransactionResponseDto.class);

        assertEquals(expected, dtoResp);
        verify(accountTransactionsService,times(1)).withdraw(dto.getAccountNumber(),dto.getAmount());
        verifyNoMoreInteractions(accountTransactionsService);
    }
    @Test
    void depositSuccess() throws Exception {
        var timestamp = TimeUtil.currentTimeMillis();
        var dto = new TransactionRequestDto(BANK_ACCOUNT_NUMBER,BigDecimal.ONE);

        var expected = new TransactionResponseDto(timestamp,BigDecimal.ONE,BANK_ACCOUNT_NUMBER, MoneyFlow.IN);
        var entity = new DepositTransaction(UUID.randomUUID(),BigDecimal.ONE,timestamp,new BankAccount(UUID.randomUUID(),BigDecimal.TEN,BANK_ACCOUNT_NUMBER),MoneyFlow.IN);
        when(accountTransactionsService.deposit(dto.getAccountNumber(),dto.getAmount())).thenReturn(entity);

        var resp = mockMvc.perform(post("/api/transaction/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        var dtoResp = objectMapper.readValue(resp.getContentAsString(),TransferTransactionResponseDto.class);

        assertEquals(expected, dtoResp);
        verify(accountTransactionsService,times(1)).deposit(dto.getAccountNumber(),dto.getAmount());
        verifyNoMoreInteractions(accountTransactionsService);
    }
    @ParameterizedTest
    @ValueSource(strings = {"-1","00011100011100011","000111000111000A"})
    void transferValidationErrorSender(String malformedAccountNUmber) throws Exception {
        var dto = new TransferTransactionRequestDto(malformedAccountNUmber,BANK_ACCOUNT_NUMBER_SECOND,BigDecimal.ONE);

        mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountTransactionsService);
    }
    @ParameterizedTest
    @ValueSource(strings = {"-1","00011100011100011","000111000111000A"})
    void transferValidationErrorReciver(String malformedAccountNUmber) throws Exception {
        var dto = new TransferTransactionRequestDto(BANK_ACCOUNT_NUMBER,malformedAccountNUmber,BigDecimal.ONE);

        mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountTransactionsService);
    }
    @ParameterizedTest
    @ValueSource(strings = {"-1"})
    void transferValidationErrorAmount(String malfromedAmoun) throws Exception {
        var dto = new TransferTransactionRequestDto(BANK_ACCOUNT_NUMBER,BANK_ACCOUNT_NUMBER_SECOND,new BigDecimal(malfromedAmoun));

        mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountTransactionsService);
    }
    @Test
    void transferValidationErrorSameAccountNumber() throws Exception {
        var dto = new TransferTransactionRequestDto(BANK_ACCOUNT_NUMBER,BANK_ACCOUNT_NUMBER,BigDecimal.ONE);

        mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountTransactionsService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1","00011100011100011","000111000111000A"})
    void withdrawValidationErrorBankAccount(String malformedAccountNUmber) throws Exception {
        var dto = new TransactionRequestDto(malformedAccountNUmber,BigDecimal.ONE);

        mockMvc.perform(post("/api/transaction/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountTransactionsService);
    }
    @ParameterizedTest
    @ValueSource(strings = {"-1"})
    void withdrawValidationErrorAmount(String malfromedAmoun) throws Exception {
        var dto = new TransactionRequestDto(BANK_ACCOUNT_NUMBER_SECOND,new BigDecimal(malfromedAmoun));

        mockMvc.perform(post("/api/transaction/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountTransactionsService);
    }
    @ParameterizedTest
    @ValueSource(strings = {"-1","00011100011100011","000111000111000A"})
    void depositValidationErrorBankAccount(String malformedAccountNUmber) throws Exception {
        var dto = new TransactionRequestDto(malformedAccountNUmber,BigDecimal.ONE);

        mockMvc.perform(post("/api/transaction/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountTransactionsService);
    }
    @ParameterizedTest
    @ValueSource(strings = {"-1"})
    void withdrawValidationErrorAmountDeposit(String malfromedAmoun) throws Exception {
        var dto = new TransactionRequestDto(BANK_ACCOUNT_NUMBER_SECOND,new BigDecimal(malfromedAmoun));

        mockMvc.perform(post("/api/transaction/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountTransactionsService);
    }
}
