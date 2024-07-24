package test.bank.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import test.bank.domain.banking.BankAccount;
import test.bank.dto.response.bankAccount.BankAccountResponseDto;
import test.bank.repository.BankAccountRepository;
import test.bank.repository.BankTransactionRepository;
import test.bank.service.AccountManagementServiceImpl;
import test.bank.service.interfaces.AccountManagementService;
import test.bank.service.interfaces.AccountNumberGeneratorService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
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
    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";
    private final String BANK_ACCOUNT_NUMBER_SECOND = "0001110001110002";
    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    public void setUp(){
        bankTransactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
    }
    @ParameterizedTest
    @ValueSource(strings = {"0", "1"})
    void createBankAccountSuccess(String initialBalanceStr) throws Exception {
        var initialBalanceExpected = new BigDecimal(initialBalanceStr).setScale(2, RoundingMode.HALF_UP);

        when(accountNumberGeneratorService.generateAccountNumber()).thenReturn(BANK_ACCOUNT_NUMBER);

        String requestBody = "{\"initialBalance\":" + initialBalanceStr + "}";
        var resp = mockMvc.perform(post("/api/management/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var dtoResp = objectMapper.readValue(resp.getContentAsString(),BankAccountResponseDto.class);

        assertEquals(initialBalanceExpected,dtoResp.getBalance());
        assertEquals(BANK_ACCOUNT_NUMBER,dtoResp.getAccountNumber());
        assertNotEquals(null,dtoResp.getId());
    }

    @Test
    void findAllSuccess() throws Exception{
        bankAccountRepository.save(new BankAccount(BigDecimal.ZERO,BANK_ACCOUNT_NUMBER));
        bankAccountRepository.save(new BankAccount(BigDecimal.ZERO,BANK_ACCOUNT_NUMBER_SECOND));

        var expectedSize = bankAccountRepository.findAll().size();
        var expectedArray = bankAccountRepository.findAll().stream().map(BankAccountResponseDto::of).toArray();

        var resp = mockMvc.perform(get("/api/management/all"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var listResp = objectMapper.readValue(resp.getContentAsString(),new TypeReference<List<BankAccountResponseDto>>(){});

        assertEquals(expectedSize, listResp.size());
        assertArrayEquals(expectedArray,listResp.toArray());
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
