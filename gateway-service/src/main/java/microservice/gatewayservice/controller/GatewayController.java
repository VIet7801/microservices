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
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = gateway.createOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
    }

    @GetMapping("/orders")
    public List<OrderResponse> orders(@RequestParam(required = false) String customerId) {
        return gateway.orders(customerId);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse order(@PathVariable UUID id) {
        return gateway.order(id);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable UUID id) {
        return gateway.cancelOrder(id);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse product = gateway.createProduct(request);
        return ResponseEntity.created(URI.create("/api/products/" + product.sku())).body(product);
    }

    @GetMapping("/products")
    public List<ProductResponse> products() {
        return gateway.products();
    }

    @GetMapping("/products/{sku}")
    public ProductResponse product(@PathVariable String sku) {
        return gateway.product(sku);
    }

    @PutMapping("/products/{sku}")
    public ProductResponse updateProduct(@PathVariable String sku, @Valid @RequestBody UpdateProductRequest request) {
        return gateway.updateProduct(sku, request);
    }

    @DeleteMapping("/products/{sku}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String sku) {
        gateway.deleteProduct(sku);
        return ResponseEntity.noContent().build();
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
