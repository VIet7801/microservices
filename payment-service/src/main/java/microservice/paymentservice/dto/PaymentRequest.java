package microservice.paymentservice.dto;

import java.util.UUID;

public record PaymentRequest(UUID orderId, boolean declined) {}
