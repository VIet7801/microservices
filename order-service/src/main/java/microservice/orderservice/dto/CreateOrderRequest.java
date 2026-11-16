package microservice.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank @Size(max = 100) String sku,
        @Positive int quantity
) {}
