package microservice.gatewayservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal price,
        @PositiveOrZero int available
) {}
