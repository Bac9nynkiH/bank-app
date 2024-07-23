package test.bank.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import test.bank.domain.banking.BankAccount;
import test.bank.dto.bankAccount.BankAccountResponseDto;
import test.bank.repository.BankAccountRepository;
import test.bank.service.AccountManagementServiceImpl;
import test.bank.service.interfaces.AccountManagementService;
import test.bank.service.interfaces.AccountNumberGeneratorService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountManagementControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @MockBean
    private AccountNumberGeneratorService accountNumberGeneratorService;
    @Autowired
    private AccountManagementService accountManagementService;

    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";
    private final String BANK_ACCOUNT_NUMBER_SECOND = "0001110001110002";
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        bankAccountRepository.deleteAll();
    }
    @Test
    void createBankAccountSuccess() throws Exception {
        when(accountNumberGeneratorService.generateAccountNumber()).thenReturn(BANK_ACCOUNT_NUMBER);
        String requestBody = "{\"initialBalance\": 1000}";

        mockMvc.perform(post("/api/management/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"balance\": 1000, \"accountNumber\": \"" + accountNumberGeneratorService.generateAccountNumber() + "\"}"));
    }

    @Test
    void createBankAccountValidationError() throws Exception {
        String requestBody = "{\"initialBalance\": -100}";

        mockMvc.perform(post("/api/management/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllSuccess() throws Exception{
        var firstBa = bankAccountRepository.save(new BankAccount(BigDecimal.ZERO,BANK_ACCOUNT_NUMBER));
        var secondBa = bankAccountRepository.save(new BankAccount(BigDecimal.ZERO,BANK_ACCOUNT_NUMBER_SECOND));

        var resp = mockMvc.perform(get("/api/management/all"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        var listResp = objectMapper.readValue(resp.getContentAsString(),new TypeReference<List<BankAccountResponseDto>>(){});
        assertEquals(listResp.size(), 2);
        assertArrayEquals(Stream.of(firstBa,secondBa).map(BankAccountResponseDto::of).toArray(),listResp.toArray());
    }

    @Test
    void getByAccountNumberSuccess() throws Exception {
        var firstBa = bankAccountRepository.save(new BankAccount(BigDecimal.ZERO,BANK_ACCOUNT_NUMBER));

        var resp = mockMvc.perform(get("/api/management/" + BANK_ACCOUNT_NUMBER))
                .andExpect(status().isOk()).andReturn().getResponse();
        var dtoResp = objectMapper.readValue(resp.getContentAsString(),BankAccountResponseDto.class);

        assertEquals(BankAccountResponseDto.of(firstBa),dtoResp);
    }

    @Test
    void getByAccountNumberNotFound() throws Exception {
        bankAccountRepository.save(new BankAccount(BigDecimal.ZERO,BANK_ACCOUNT_NUMBER));

        mockMvc.perform(get("/api/management/" + BANK_ACCOUNT_NUMBER_SECOND))
                .andExpect(status().isNotFound());
    }
}
