package microservice.paymentservice.model;

import java.util.UUID;

public record Payment(UUID orderId, String status) {}
