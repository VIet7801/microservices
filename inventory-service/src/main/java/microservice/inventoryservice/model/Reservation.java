package microservice.inventoryservice.model;

import java.util.UUID;

public record Reservation(UUID orderId, String sku, int quantity, String status) {}
