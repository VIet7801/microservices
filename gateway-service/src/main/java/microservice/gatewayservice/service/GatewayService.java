package microservice.gatewayservice.service;

import microservice.gatewayservice.dto.*;
import microservice.gatewayservice.grpc.GrpcClients;
import microservice.grpc.api.CreateOrderInput;
import microservice.grpc.api.Empty;
import microservice.grpc.api.OrderId;
import microservice.grpc.api.OrderReply;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GatewayService {
    private final GrpcClients clients;

    public GatewayService(GrpcClients clients) {
        this.clients = clients;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        return toOrder(clients.orders().createOrder(CreateOrderInput.newBuilder().setSku(request.sku())
                .setQuantity(request.quantity()).setPaymentDeclined(request.paymentDeclined()).build()));
    }

    public List<OrderResponse> orders() {
        return clients.orders().listOrders(Empty.getDefaultInstance()).getOrdersList().stream()
                .map(GatewayService::toOrder).toList();
    }

    public OrderResponse order(UUID id) {
        return toOrder(clients.orders().getOrder(orderId(id)));
    }

    public List<StockResponse> stocks() {
        return clients.inventory().listStock(Empty.getDefaultInstance()).getStocksList().stream()
                .map(stock -> new StockResponse(stock.getSku(), stock.getAvailable())).toList();
    }

    public ReservationResponse reservation(UUID id) {
        var result = clients.inventory().getReservation(orderId(id));
        return new ReservationResponse(result.getOrderId(), result.getSku(), result.getQuantity(), result.getStatus());
    }

    public PaymentResponse payment(UUID id) {
        var result = clients.payments().getPayment(orderId(id));
        return new PaymentResponse(result.getOrderId(), result.getStatus());
    }

    private static OrderId orderId(UUID id) {
        return OrderId.newBuilder().setId(id.toString()).build();
    }

    private static OrderResponse toOrder(OrderReply order) {
        return new OrderResponse(order.getId(), order.getSku(), order.getQuantity(), order.getStatus());
    }
}
