package microservice.paymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(UUID orderId, boolean declined, BigDecimal amount) {}
