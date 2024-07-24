package test.bank.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BankApplicationException extends RuntimeException {
    public BankApplicationException(String message) {
        super(message);
    }
}
