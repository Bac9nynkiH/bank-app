package test.bank.exception;

public class BankApplicationNotFoundException extends BankApplicationException {
    public BankApplicationNotFoundException(String message) {
        super(message);
    }

    public BankApplicationNotFoundException() {

    }
}
