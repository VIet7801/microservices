package microservice.grpc.support;

import io.grpc.Status;
import microservice.common.exception.BusinessConflictException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
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

    public static <T> void respond(StreamObserver<T> observer, Supplier<T> action) {
        T result;
        try {
            result = action.get();
        } catch (IllegalArgumentException e) {
            observer.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            return;
        } catch (NoSuchElementException e) {
            observer.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
            return;
        } catch (BusinessConflictException e) {
            observer.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
            return;
        } catch (Exception e) {
            log.error("gRPC request failed", e);
            observer.onError(Status.INTERNAL.withDescription("Cannot process request").asRuntimeException());
            return;
        }
        observer.onNext(result);
        observer.onCompleted();
    }
}
