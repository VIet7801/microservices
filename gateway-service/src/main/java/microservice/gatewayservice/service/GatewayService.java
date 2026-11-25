package microservice.gatewayservice.service;

import microservice.gatewayservice.dto.*;
import microservice.gatewayservice.grpc.GrpcClients;
import microservice.grpc.api.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class GatewayService {
    private final GrpcClients clients;

    public GatewayService(GrpcClients clients) {
        this.clients = clients;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        return toOrder(clients.orders().createOrder(CreateOrderInput.newBuilder()
                .setCustomerId(request.customerId()).setSku(request.sku())
                .setQuantity(request.quantity()).setPaymentDeclined(request.paymentDeclined()).build()));
    }

    public List<OrderResponse> orders(String customerId) {
        var input = ListOrdersInput.newBuilder().setCustomerId(customerId == null ? "" : customerId).build();
        return clients.orders().listOrders(input).getOrdersList().stream().map(GatewayService::toOrder).toList();
    }

    public OrderResponse order(UUID id) {
        return toOrder(clients.orders().getOrder(orderId(id)));
    }

    public OrderResponse cancelOrder(UUID id) {
        return toOrder(clients.orders().cancelOrder(orderId(id)));
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        return toProduct(clients.inventory().createProduct(ProductInput.newBuilder()
                .setSku(request.sku()).setName(request.name())
                .setPrice(request.price().toPlainString()).setAvailable(request.available()).build()));
    }

    public List<ProductResponse> products() {
        return clients.inventory().listProducts(Empty.getDefaultInstance()).getProductsList().stream()
                .map(GatewayService::toProduct).toList();
    }

    public ProductResponse product(String sku) {
        return toProduct(clients.inventory().getProduct(Sku.newBuilder().setSku(sku).build()));
    }

    public ProductResponse updateProduct(String sku, UpdateProductRequest request) {
        return toProduct(clients.inventory().updateProduct(ProductInput.newBuilder()
                .setSku(sku).setName(request.name())
                .setPrice(request.price().toPlainString()).setAvailable(request.available()).build()));
    }

    public void deleteProduct(String sku) {
        clients.inventory().deleteProduct(Sku.newBuilder().setSku(sku).build());
    }

    public ReservationResponse reservation(UUID id) {
        var result = clients.inventory().getReservation(orderId(id));
        return new ReservationResponse(result.getOrderId(), result.getSku(), result.getQuantity(),
                result.getStatus(), money(result.getAmount()));
    }

    public PaymentResponse payment(UUID id) {
        var result = clients.payments().getPayment(orderId(id));
        return new PaymentResponse(result.getOrderId(), result.getStatus(), money(result.getAmount()));
    }

    private static OrderId orderId(UUID id) {
        return OrderId.newBuilder().setId(id.toString()).build();
    }

    private static BigDecimal money(String value) {
        return value.isEmpty() ? null : new BigDecimal(value);
    }

    private static OrderResponse toOrder(OrderReply order) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getSku(), order.getQuantity(),
                money(order.getTotalAmount()), order.getStatus());
    }

    private static ProductResponse toProduct(ProductReply product) {
        return new ProductResponse(product.getSku(), product.getName(), money(product.getPrice()),
                product.getAvailable(), product.getActive());
    }
}
