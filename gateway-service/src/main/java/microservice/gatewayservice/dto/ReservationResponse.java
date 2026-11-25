package microservice.gatewayservice.dto;

import java.math.BigDecimal;

public record ReservationResponse(String orderId, String sku, int quantity, String status, BigDecimal amount) {}
