package test.bank.exception;

public class BankApplicationNegativeBalanceException extends BankApplicationException {
    public BankApplicationNegativeBalanceException(String message) {
        super(message);
    }
}
