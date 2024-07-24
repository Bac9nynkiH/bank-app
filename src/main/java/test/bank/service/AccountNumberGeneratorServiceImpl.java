package test.bank.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import test.bank.service.interfaces.AccountNumberGeneratorService;

import java.math.BigInteger;
import java.util.UUID;

@Service
public class AccountNumberGeneratorServiceImpl implements AccountNumberGeneratorService {
    @Value("${app.bank.account-number-length:16}")
    private Integer accNumberLength;
    @Override
    public String generateAccountNumber() {
        return String.format("%010d",new BigInteger(UUID.randomUUID().toString().replace("-",""),16)).substring(0,accNumberLength);
    }
}
