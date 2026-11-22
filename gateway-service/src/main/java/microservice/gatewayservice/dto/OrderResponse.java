package microservice.gatewayservice.dto;

public record OrderResponse(String id, String sku, int quantity, String status) {}
