package test.bank.unit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PersistenceException;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.bank.controller.AccountManagementController;
import test.bank.domain.banking.BankAccount;
import test.bank.dto.bankAccount.BankAccountCreateDto;
import test.bank.dto.bankAccount.BankAccountResponseDto;
import test.bank.exception.BankApplicationNotFoundException;
import test.bank.service.interfaces.AccountManagementService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AccountManagementController.class)
class AccountManagementControllerTest {
    private final String BANK_ACCOUNT_NUMBER = "0001110001110001";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountManagementService accountManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {"0", "1"})
    void createBankAccountSuccess(String initialBalanceStr) throws Exception {
        BigDecimal initialBalance = new BigDecimal(initialBalanceStr);
        var dto = new BankAccountCreateDto(initialBalance);

        var expected = new BankAccountResponseDto(UUID.randomUUID(),initialBalance,BANK_ACCOUNT_NUMBER);
        var entity = new BankAccount(initialBalance,BANK_ACCOUNT_NUMBER);
        when(accountManagementService.createBankAccount(initialBalance)).thenReturn(entity);

        mockMvc.perform(post("/api/management/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(expected.getAccountNumber()))
                .andExpect(jsonPath("$.balance").value(expected.getBalance()));

        verify(accountManagementService,times(1)).createBankAccount(initialBalance);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1"})
    void createBankAccountValidationError(String initialBalanceStr) throws Exception {
        BigDecimal initialBalance = new BigDecimal(initialBalanceStr);
        var dto = new BankAccountCreateDto(initialBalance);

        mockMvc.perform(post("/api/management/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(accountManagementService,times(0)).createBankAccount(initialBalance);
        verifyNoMoreInteractions(accountManagementService);
    }

    @Test
    void findAllSuccess() throws Exception {
        var id = UUID.randomUUID();
        List<BankAccount> expected = List.of(new BankAccount(id,BigDecimal.ZERO,BANK_ACCOUNT_NUMBER));
        when(accountManagementService.findAll()).thenReturn(expected);

        mockMvc.perform(get("/api/management/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].balance").value(BigDecimal.ZERO))
                .andExpect(jsonPath("$[0].accountNumber").value(BANK_ACCOUNT_NUMBER))
                .andExpect(jsonPath("$[0].id").value(id.toString()));

        verify(accountManagementService,times(1)).findAll();
    }

    @Test
    void getByAccountNumberSuccess() throws Exception {
        var id = UUID.randomUUID();
        BankAccount expected = new BankAccount(id,BigDecimal.ZERO,BANK_ACCOUNT_NUMBER);
        when(accountManagementService.getByAccountNumber(BANK_ACCOUNT_NUMBER)).thenReturn(expected);

        mockMvc.perform(get("/api/management/" + BANK_ACCOUNT_NUMBER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
                .andExpect(jsonPath("$.accountNumber").value(BANK_ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(accountManagementService,times(1)).getByAccountNumber(BANK_ACCOUNT_NUMBER);
    }
    @Test
    void getByAccountNumberNotFoundError() throws Exception {
        when(accountManagementService.getByAccountNumber(BANK_ACCOUNT_NUMBER)).thenThrow(new BankApplicationNotFoundException());

        mockMvc.perform(get("/api/management/" + BANK_ACCOUNT_NUMBER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("entity not found"));

        verify(accountManagementService,times(1)).getByAccountNumber(BANK_ACCOUNT_NUMBER);
    }
}