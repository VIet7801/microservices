package microservice.gatewayservice.dto;

import java.math.BigDecimal;

public record OrderResponse(String id, String customerId, String sku, int quantity,
                            BigDecimal totalAmount, String status) {}
