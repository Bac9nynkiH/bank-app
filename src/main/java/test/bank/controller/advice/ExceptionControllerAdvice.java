package test.bank.controller.advice;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import test.bank.exception.ApiError;
import test.bank.exception.BankApplicationBadRequestException;
import test.bank.exception.BankApplicationException;
import test.bank.exception.BankApplicationNotFoundException;

import java.sql.SQLException;

@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            errors.append(errorMessage).append("; ");
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.from(HttpStatus.BAD_REQUEST, "validation error", errors.toString(), ((ServletWebRequest) request).getRequest().getRequestURI()));
    }
    @ExceptionHandler(value = {BankApplicationNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<ApiError> handleBankApplicationNotFoundException(HttpServletRequest req, BankApplicationNotFoundException ex) {
        log.warn("[handleBankApplicationNotFoundException] exception: {}", ex.getMessage());

        var bodyOfResponse = "entity not found";
        var error = "entity not found";

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.from(HttpStatus.NOT_FOUND, error, bodyOfResponse, req.getRequestURI()));
    }
    @ExceptionHandler(value = {BankApplicationBadRequestException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<ApiError> handleBankApplicationBadRequestException(HttpServletRequest req, BankApplicationBadRequestException ex) {
        log.warn("[handleBankApplicationBadRequestException] exception: {}", ex.getMessage());

        var bodyOfResponse = ex.getMessage();
        var error = "bad request";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.from(HttpStatus.BAD_REQUEST, error, bodyOfResponse, req.getRequestURI()));
    }
    @ExceptionHandler(value = {BankApplicationException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<ApiError> handleBankApplicationException(HttpServletRequest req, BankApplicationBadRequestException ex) {
        log.warn("[handleBankApplicationBadRequestException] exception: {}", ex.getMessage());

        var bodyOfResponse = "internal server error";
        var error = "internal server error";

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.from(HttpStatus.INTERNAL_SERVER_ERROR, error, bodyOfResponse, req.getRequestURI()));
    }

    @ExceptionHandler(value = {PersistenceException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<ApiError> handlePersistenceException(HttpServletRequest req, PersistenceException ex) {
        log.warn("[handlePersistenceException] exception: {}", ex.getMessage());

        var bodyOfResponse = "db request failed";
        var error = "db request failed";

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.from(HttpStatus.INTERNAL_SERVER_ERROR, error, bodyOfResponse, req.getRequestURI()));
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiError> handleThrowable(HttpServletRequest req, Throwable t) {
        log.error("handleThrowable: unexpected common exception [{}]: {}", t.getClass(), t.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.from(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error", "request support", req.getRequestURI()));
    }
}
