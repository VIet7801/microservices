package microservice.gatewayservice.controller;

import jakarta.validation.Valid;
import microservice.gatewayservice.dto.*;
import microservice.gatewayservice.service.GatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class GatewayController {
    private final GatewayService gateway;

    public GatewayController(GatewayService gateway) {
        this.gateway = gateway;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = gateway.createOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
    }

    @GetMapping("/orders/all")
    public List<OrderResponse> all() {
        return gateway.orders();
    }

    @GetMapping("/orders/{id}")
    public OrderResponse find(@PathVariable UUID id) {
        return gateway.order(id);
    }

    @GetMapping("/inventory")
    public List<StockResponse> stocks() {
        return gateway.stocks();
    }

    @GetMapping("/inventory/reservations/{id}")
    public ReservationResponse reservation(@PathVariable UUID id) {
        return gateway.reservation(id);
    }

    @GetMapping("/payments/{id}")
    public PaymentResponse payment(@PathVariable UUID id) {
        return gateway.payment(id);
    }
}
