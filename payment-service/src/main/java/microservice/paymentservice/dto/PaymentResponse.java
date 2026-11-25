package microservice.paymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(UUID orderId, String status, BigDecimal amount) {}
