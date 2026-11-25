package microservice.gatewayservice.dto;

import java.math.BigDecimal;

public record PaymentResponse(String orderId, String status, BigDecimal amount) {}
