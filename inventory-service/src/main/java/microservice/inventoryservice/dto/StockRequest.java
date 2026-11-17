package microservice.inventoryservice.dto;

import java.util.UUID;

public record StockRequest(UUID orderId, String sku, int quantity) {}
