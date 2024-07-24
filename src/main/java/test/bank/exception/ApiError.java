package test.bank.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import test.bank.util.TimeUtil;

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
                TimeUtil.currentTimeMillis(),
                status.value(),
                error,
                message,
                originPath);
    }
}