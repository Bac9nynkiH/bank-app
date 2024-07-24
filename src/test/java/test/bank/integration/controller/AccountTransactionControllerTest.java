package test.bank.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import test.bank.domain.banking.BankAccount;
import test.bank.domain.banking.transaction.DepositTransaction;
import test.bank.domain.banking.transaction.MoneyFlow;
import test.bank.dto.request.transaction.TransactionRequestDto;
import test.bank.dto.request.transaction.TransferTransactionRequestDto;
import test.bank.dto.response.transaction.TransactionResponseDto;
import test.bank.dto.response.transaction.TransferTransactionResponseDto;
import test.bank.repository.BankAccountRepository;
import test.bank.repository.BankTransactionRepository;
import test.bank.repository.DepositTransactionRepository;
import test.bank.repository.TransferTransactionRepository;
import test.bank.service.interfaces.AccountTransactionsService;
import test.bank.util.TimeUtil;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountTransactionControllerTest {
    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";
    private final String BANK_ACCOUNT_NUMBER_SECOND = "0001110001110002";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountTransactionsService accountTransactionsService;
    @Autowired
    private BankTransactionRepository bankTransactionRepository;
    @Autowired
    private DepositTransactionRepository depositTransactionRepository;
    @Autowired
    private TransferTransactionRepository transferTransactionRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    public void cleanUp() {
        bankTransactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        var bankAccount1 = bankAccountRepository.save(new BankAccount(BigDecimal.TEN, BANK_ACCOUNT_NUMBER));
        depositTransactionRepository.save(new DepositTransaction(BigDecimal.TEN, System.currentTimeMillis(), bankAccount1, MoneyFlow.IN));

        var bankAccount2 = bankAccountRepository.save(new BankAccount(BigDecimal.TEN, BANK_ACCOUNT_NUMBER_SECOND));
        depositTransactionRepository.save(new DepositTransaction(BigDecimal.TEN, System.currentTimeMillis(), bankAccount2, MoneyFlow.IN));
    }

    @Test
    void transferSuccess() throws Exception {
        try (MockedStatic mocked = mockStatic(TimeUtil.class)) {
            mocked.when(TimeUtil::currentTimeMillis).thenReturn(100L);
            var timestamp = TimeUtil.currentTimeMillis();
            var dto = new TransferTransactionRequestDto(BANK_ACCOUNT_NUMBER, BANK_ACCOUNT_NUMBER_SECOND, BigDecimal.ONE);

            var expected = new TransferTransactionResponseDto(timestamp, BigDecimal.ONE, BANK_ACCOUNT_NUMBER, MoneyFlow.OUT, BANK_ACCOUNT_NUMBER_SECOND);

            var resp = mockMvc.perform(post("/api/transaction/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse();
            var dtoResp = objectMapper.readValue(resp.getContentAsString(), TransferTransactionResponseDto.class);

            assertEquals(expected, dtoResp);
        }
    }

    @Test
    void withdrawSuccess() throws Exception {
        try (MockedStatic mocked = mockStatic(TimeUtil.class)) {
            mocked.when(TimeUtil::currentTimeMillis).thenReturn(100L);

            var timestamp = TimeUtil.currentTimeMillis();
            var dto = new TransactionRequestDto(BANK_ACCOUNT_NUMBER, BigDecimal.ONE);

            var expected = new TransactionResponseDto(timestamp, BigDecimal.ONE, BANK_ACCOUNT_NUMBER, MoneyFlow.OUT);

            var resp = mockMvc.perform(post("/api/transaction/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse();
            var dtoResp = objectMapper.readValue(resp.getContentAsString(), TransferTransactionResponseDto.class);

            assertEquals(expected, dtoResp);
        }

    }

    @Test
    void depositSuccess() throws Exception {
        try (MockedStatic mocked = mockStatic(TimeUtil.class)) {
            mocked.when(TimeUtil::currentTimeMillis).thenReturn(100L);

            var timestamp = TimeUtil.currentTimeMillis();
            var dto = new TransactionRequestDto(BANK_ACCOUNT_NUMBER, BigDecimal.ONE);

            var expected = new TransactionResponseDto(timestamp, BigDecimal.ONE, BANK_ACCOUNT_NUMBER, MoneyFlow.IN);

            var resp = mockMvc.perform(post("/api/transaction/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse();
            var dtoResp = objectMapper.readValue(resp.getContentAsString(), TransferTransactionResponseDto.class);

            assertEquals(expected, dtoResp);
        }

    }

}
