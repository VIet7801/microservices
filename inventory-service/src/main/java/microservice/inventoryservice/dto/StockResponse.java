package microservice.inventoryservice.dto;

import java.util.UUID;

public record StockResponse(UUID orderId, String sku, int quantity, String status) {}
