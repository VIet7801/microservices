package microservice.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        String sku,
        int quantity,
        BigDecimal totalAmount,
        String status) {}
