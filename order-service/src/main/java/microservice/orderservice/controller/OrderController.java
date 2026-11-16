package microservice.orderservice.controller;

import jakarta.validation.Valid;
import microservice.orderservice.dto.CreateOrderRequest;
import microservice.orderservice.dto.OrderResponse;
import microservice.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orders.create(request.sku(), request.quantity());
        return ResponseEntity.created(URI.create("/orders/" + order.id())).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.of(orders.findById(id));
    }
}
