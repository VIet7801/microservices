package microservice.inventoryservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockResponse(UUID orderId, String sku, int quantity, String status, BigDecimal amount) {}
