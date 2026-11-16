package microservice.orderservice.application.port.out;

import microservice.orderservice.domain.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    void insert(Order order);
    Optional<Order> findById(UUID id);
}