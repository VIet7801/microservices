package microservice.orderservice.service;

import microservice.orderservice.dto.OrderResponse;
import microservice.orderservice.entity.OrderEntity;
import microservice.orderservice.repository.OrderRepository;
import microservice.orderservice.repository.SagaRepository;
import microservice.messaging.outbox.Outbox;
import microservice.messaging.dto.SagaMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository repository;
    private final SagaRepository sagas;
    private final Outbox outbox;

    public OrderService(OrderRepository repository, SagaRepository sagas, Outbox outbox) {
        this.repository = repository;
        this.sagas = sagas;
        this.outbox = outbox;
    }

    @Transactional
    public OrderResponse create(String sku, int quantity, boolean paymentDeclined) {
        OrderEntity order = repository.save(OrderEntity.pending(sku, quantity));
        sagas.start(order.getId(), paymentDeclined);
        outbox.add("inventory-commands", SagaMessage.of(order.getId(), "ReserveStock", sku, quantity, paymentDeclined));
        return toResponse(order);
    }

    public List<OrderResponse> getAll() {
        return repository.findAll().stream().map(OrderService::toResponse).toList();
    }

    public Optional<OrderResponse> findById(UUID id) {
        return repository.findById(id).map(OrderService::toResponse);
    }

    private static OrderResponse toResponse(OrderEntity order) {
        return new OrderResponse(order.getId(), order.getSku(), order.getQuantity(), order.getStatus().name());
    }
}
