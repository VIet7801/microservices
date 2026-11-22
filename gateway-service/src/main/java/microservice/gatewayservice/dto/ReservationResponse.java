package microservice.gatewayservice.dto;

public record ReservationResponse(String orderId, String sku, int quantity, String status) {}
