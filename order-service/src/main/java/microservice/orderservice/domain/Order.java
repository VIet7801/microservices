package microservice.orderservice.domain;

import java.util.UUID;

public record Order(UUID id, String sku, int quantity, Status status) {

    public enum Status { PENDING }

    public static Order pending(String sku, int quantity) {
        if (sku == null || sku.isBlank() || sku.length() > 100) {
            throw new IllegalArgumentException("SKU must contain 1 to 100 characters");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return new Order(UUID.randomUUID(), sku, quantity, Status.PENDING);
    }
}