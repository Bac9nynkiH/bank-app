package test.bank.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import test.bank.controller.AccountManagementController;
import test.bank.domain.banking.BankAccount;
import test.bank.dto.request.bankAccount.BankAccountCreateRequestDto;
import test.bank.dto.response.bankAccount.BankAccountResponseDto;
import test.bank.exception.BankApplicationNotFoundException;
import test.bank.service.interfaces.AccountManagementService;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        var dto = new BankAccountCreateRequestDto(initialBalance);

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
        verifyNoMoreInteractions(accountManagementService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1"})
    void createBankAccountValidationError(String initialBalanceStr) throws Exception {
        BigDecimal initialBalance = new BigDecimal(initialBalanceStr);
        var dto = new BankAccountCreateRequestDto(initialBalance);

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
                .andExpect(jsonPath("$[0].balance").value(BigDecimal.ZERO.setScale(2,RoundingMode.HALF_UP)))
                .andExpect(jsonPath("$[0].accountNumber").value(BANK_ACCOUNT_NUMBER))
                .andExpect(jsonPath("$[0].id").value(id.toString()));

        verify(accountManagementService,times(1)).findAll();
        verifyNoMoreInteractions(accountManagementService);
    }

    @Test
    void getByAccountNumberSuccess() throws Exception {
        var id = UUID.randomUUID();
        BankAccount expected = new BankAccount(id,BigDecimal.ZERO,BANK_ACCOUNT_NUMBER);
        when(accountManagementService.getByAccountNumber(BANK_ACCOUNT_NUMBER)).thenReturn(expected);

        mockMvc.perform(get("/api/management/" + BANK_ACCOUNT_NUMBER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)))
                .andExpect(jsonPath("$.accountNumber").value(BANK_ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(accountManagementService,times(1)).getByAccountNumber(BANK_ACCOUNT_NUMBER);
        verifyNoMoreInteractions(accountManagementService);
    }
    @Test
    void getByAccountNumberNotFoundError() throws Exception {
        when(accountManagementService.getByAccountNumber(BANK_ACCOUNT_NUMBER)).thenThrow(new BankApplicationNotFoundException());

        mockMvc.perform(get("/api/management/" + BANK_ACCOUNT_NUMBER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("entity not found"));

        verify(accountManagementService,times(1)).getByAccountNumber(BANK_ACCOUNT_NUMBER);
        verifyNoMoreInteractions(accountManagementService);
    }
}