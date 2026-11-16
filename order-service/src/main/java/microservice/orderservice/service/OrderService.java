package microservice.orderservice.service;

import microservice.orderservice.dto.OrderResponse;
import microservice.orderservice.entity.OrderEntity;
import microservice.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderResponse create(String sku, int quantity) {
        return toResponse(repository.save(OrderEntity.pending(sku, quantity)));
    }

    public Optional<OrderResponse> findById(UUID id) {
        return repository.findById(id).map(OrderService::toResponse);
    }

    private static OrderResponse toResponse(OrderEntity order) {
        return new OrderResponse(order.getId(), order.getSku(), order.getQuantity(), order.getStatus().name());
    }
}
