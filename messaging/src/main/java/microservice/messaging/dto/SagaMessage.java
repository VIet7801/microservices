package microservice.messaging.dto;

import java.util.UUID;

public record SagaMessage(UUID messageId, UUID orderId, String type, String sku,
                          int quantity, boolean paymentDeclined) {
    public SagaMessage {
        if (messageId == null || orderId == null || type == null || type.isBlank()
                || sku == null || sku.isBlank() || sku.length() > 100 || quantity < 1) {
            throw new IllegalArgumentException("Invalid saga message");
        }
    }

    public static SagaMessage of(UUID orderId, String type, String sku, int quantity, boolean declined) {
        return new SagaMessage(UUID.randomUUID(), orderId, type, sku, quantity, declined);
    }

    public SagaMessage reply(String result) {
        return of(orderId, result, sku, quantity, paymentDeclined);
    }
}
