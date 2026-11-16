package microservice.orderservice.application;

import microservice.orderservice.application.port.in.OrderUseCase;
import microservice.orderservice.application.port.out.OrderRepository;
import microservice.orderservice.domain.Order;
import java.util.Optional;
import java.util.UUID;

public class OrderService implements OrderUseCase {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order create(String sku, int quantity) {
        Order order = Order.pending(sku, quantity);
        repository.insert(order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return repository.findById(id);
    }
}