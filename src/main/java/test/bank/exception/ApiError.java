package test.bank.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ApiError {
    private long timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public static ApiError from(HttpStatus status,
                                String error,
                                @Nullable String message,
                                String originPath) {
        return new ApiError(
                System.currentTimeMillis(),
                status.value(),
                error,
                message,
                originPath);
    }

    public static ApiError invalid(HttpStatus status, String originPath, Set<String> violations) {
        final String message = "Validation Error" + ": " +
                               Arrays.toString(Collections.singletonList(violations).toArray())
                                       .replaceAll("\\[", "").replaceAll("]", "") +
                               ".";
        return new ApiError(
                System.currentTimeMillis(),
                status.value(), status.getReasonPhrase(),
                message, originPath);
    }
}