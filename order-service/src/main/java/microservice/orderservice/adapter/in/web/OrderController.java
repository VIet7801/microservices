package microservice.orderservice.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import microservice.orderservice.application.port.in.OrderUseCase;
import microservice.orderservice.domain.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderUseCase orders;

    public OrderController(OrderUseCase orders) {
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orders.create(request.sku(), request.quantity());
        return ResponseEntity.created(URI.create("/orders/" + order.id())).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.of(orders.findById(id));
    }

    public record CreateOrderRequest(
            @NotBlank @Size(max = 100) String sku,
            @Positive int quantity
    ) {}
}