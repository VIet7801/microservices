package microservice.grpc.support;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import microservice.common.exception.BusinessConflictException;
import microservice.common.exception.InvalidStateException;
import microservice.common.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Supplier;

public final class GrpcErrors {
    private static final Logger log = LoggerFactory.getLogger(GrpcErrors.class);

    private GrpcErrors() {}

    public static UUID orderId(String value) {
        try {
            UUID id = UUID.fromString(value);
            if (!id.toString().equalsIgnoreCase(value)) {
                throw new IllegalArgumentException();
            }
            return id;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("orderId must be a UUID");
        }
    }

    public static BigDecimal money(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount must be a decimal number");
        }
    }

    public static String money(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    public static <T> void respond(StreamObserver<T> observer, Supplier<T> action) {
        T result;
        try {
            result = action.get();
        } catch (Exception e) {
            observer.onError(toStatus(e).asRuntimeException());
            return;
        }
        observer.onNext(result);
        observer.onCompleted();
    }

    private static Status toStatus(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage());
        }
        if (e instanceof NotFoundException) {
            return Status.NOT_FOUND.withDescription(e.getMessage());
        }
        if (e instanceof BusinessConflictException) {
            return Status.ALREADY_EXISTS.withDescription(e.getMessage());
        }
        if (e instanceof InvalidStateException) {
            return Status.FAILED_PRECONDITION.withDescription(e.getMessage());
        }
        if (e instanceof DataIntegrityViolationException violation) {
            log.warn("Database constraint violated: {}", violation.getMostSpecificCause().getMessage());
            return Status.ALREADY_EXISTS.withDescription("Data conflicts with an existing record");
        }
        if (e instanceof TransientDataAccessException) {
            log.warn("Temporary database error: {}", e.getMessage());
            return Status.UNAVAILABLE.withDescription("Database is busy, please retry");
        }
        log.error("gRPC request failed", e);
        return Status.INTERNAL.withDescription("Cannot process request");
    }
}
