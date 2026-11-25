package microservice.inventoryservice.dto;

import java.math.BigDecimal;

public record ProductResponse(String sku, String name, BigDecimal price, int available, boolean active) {}
