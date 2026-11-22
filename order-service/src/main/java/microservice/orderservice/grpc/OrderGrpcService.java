package microservice.orderservice.grpc;

import io.grpc.stub.StreamObserver;
import microservice.grpc.api.*;
import microservice.grpc.support.GrpcErrors;
import microservice.orderservice.dto.OrderResponse;
import microservice.orderservice.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class OrderGrpcService extends OrderApiGrpc.OrderApiImplBase {
    private final OrderService orders;

    public OrderGrpcService(OrderService orders) {
        this.orders = orders;
    }

    @Override
    public void createOrder(CreateOrderInput request, StreamObserver<OrderReply> observer) {
        GrpcErrors.respond(observer, () -> reply(orders.create(
                request.getSku(), request.getQuantity(), request.getPaymentDeclined())));
    }

    @Override
    public void getOrder(OrderId request, StreamObserver<OrderReply> observer) {
        GrpcErrors.respond(observer, () -> reply(orders.findById(GrpcErrors.orderId(request.getId()))
                .orElseThrow(() -> new NoSuchElementException("Order not found"))));
    }

    @Override
    public void listOrders(Empty request, StreamObserver<OrderList> observer) {
        GrpcErrors.respond(observer, () -> OrderList.newBuilder()
                .addAllOrders(orders.getAll().stream().map(OrderGrpcService::reply).toList()).build());
    }

    private static OrderReply reply(OrderResponse order) {
        return OrderReply.newBuilder().setId(order.id().toString()).setSku(order.sku())
                .setQuantity(order.quantity()).setStatus(order.status()).build();
    }
}
