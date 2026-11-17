package microservice.paymentservice.dto;

import java.util.UUID;

public record PaymentResponse(UUID orderId, String status) {}
