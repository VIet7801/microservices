package microservice.orderservice.application.port.in;

import microservice.orderservice.domain.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderUseCase {
    Order create(String sku, int quantity);
    Optional<Order> findById(UUID id);
}