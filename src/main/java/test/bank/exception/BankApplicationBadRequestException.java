package test.bank.exception;

public class BankApplicationBadRequestException extends BankApplicationException{
    public BankApplicationBadRequestException(String message) {
        super(message);
    }
}
