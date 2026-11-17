package microservice.messaging.dto;

import java.util.UUID;

public record SagaMessage(UUID messageId, UUID orderId, String type, String sku,
                          int quantity, boolean paymentDeclined) {
    public static SagaMessage of(UUID orderId, String type, String sku, int quantity, boolean declined) {
        return new SagaMessage(UUID.randomUUID(), orderId, type, sku, quantity, declined);
    }

    public SagaMessage reply(String result) {
        return of(orderId, result, sku, quantity, paymentDeclined);
    }
}
