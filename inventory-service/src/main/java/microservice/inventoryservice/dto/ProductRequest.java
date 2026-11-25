package microservice.inventoryservice.dto;

import java.math.BigDecimal;

public record ProductRequest(String sku, String name, BigDecimal price, int available) {}
