package microservice.orderservice.service;

import microservice.orderservice.cache.OrderCache;
import microservice.orderservice.dto.OrderResponse;
import microservice.orderservice.entity.OrderEntity;
import microservice.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository repository;
    private final OrderSaga saga;
    private final OrderCache cache;

    public OrderService(OrderRepository repository, OrderSaga saga, OrderCache cache) {
        this.repository = repository;
        this.saga = saga;
        this.cache = cache;
    }

    @Transactional
    public OrderResponse create(String customerId, String sku, int quantity, boolean paymentDeclined) {
        OrderEntity order = repository.save(OrderEntity.pending(customerId, sku, quantity));
        saga.start(order, paymentDeclined);
        return toResponse(order);
    }

    public List<OrderResponse> list(String customerId) {
        List<OrderEntity> orders = customerId == null || customerId.isBlank()
                ? repository.findAll()
                : repository.findByCustomerId(customerId);
        return orders.stream().map(OrderService::toResponse).toList();
    }

    public Optional<OrderResponse> findById(UUID id) {
        var cached = cache.get(id);
        if (cached.isPresent()) {
            return cached;
        }
        var order = repository.findById(id).map(OrderService::toResponse);
        order.ifPresent(cache::put);
        return order;
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        return toResponse(saga.cancel(id));
    }

    private static OrderResponse toResponse(OrderEntity order) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getSku(), order.getQuantity(),
                order.getTotalAmount(), order.getStatus().name());
    }
}
