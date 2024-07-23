package test.bank.exception;

public class BankApplicationInternalServerErrorException extends BankApplicationException{
    public BankApplicationInternalServerErrorException(String message) {
        super(message);
    }
}
