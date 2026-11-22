package microservice.gatewayservice.exception;

import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ApiError> grpc(StatusRuntimeException e, HttpServletRequest request) {
        HttpStatus status = switch (e.getStatus().getCode()) {
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS, FAILED_PRECONDITION -> HttpStatus.CONFLICT;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        String message = e.getStatus().getDescription();
        if (status.is5xxServerError()) {
            log.error("Downstream gRPC request failed: {}", e.getStatus());
            message = status == HttpStatus.INTERNAL_SERVER_ERROR ? "Cannot process request" : "Service temporarily unavailable";
        }
        return error(status, e.getStatus().getCode().name(), message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .sorted().distinct().collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", message, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> malformed(Exception e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "Invalid JSON body or parameter", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest request) {
        if (e instanceof ErrorResponse response) {
            HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
            return error(status, status.name(), status.getReasonPhrase(), request);
        }
        log.error("HTTP request failed", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL", "Cannot process request", request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }

    public record ApiError(Instant timestamp, int status, String code, String message, String path) {}
}
