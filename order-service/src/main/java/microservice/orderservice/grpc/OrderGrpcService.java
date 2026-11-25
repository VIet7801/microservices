package microservice.orderservice.grpc;

import io.grpc.stub.StreamObserver;
import microservice.grpc.api.*;
import microservice.grpc.support.GrpcErrors;
import microservice.orderservice.dto.OrderResponse;
import microservice.orderservice.service.OrderService;
import org.springframework.stereotype.Component;

import microservice.common.exception.NotFoundException;

@Component
public class OrderGrpcService extends OrderApiGrpc.OrderApiImplBase {
    private final OrderService orders;

    public OrderGrpcService(OrderService orders) {
        this.orders = orders;
    }

    @Override
    public void createOrder(CreateOrderInput request, StreamObserver<OrderReply> observer) {
        GrpcErrors.respond(observer, () -> reply(orders.create(request.getCustomerId(),
                request.getSku(), request.getQuantity(), request.getPaymentDeclined())));
    }

    @Override
    public void getOrder(OrderId request, StreamObserver<OrderReply> observer) {
        GrpcErrors.respond(observer, () -> reply(orders.findById(GrpcErrors.orderId(request.getId()))
                .orElseThrow(() -> new NotFoundException("Order not found"))));
    }

    @Override
    public void listOrders(ListOrdersInput request, StreamObserver<OrderList> observer) {
        GrpcErrors.respond(observer, () -> OrderList.newBuilder()
                .addAllOrders(orders.list(request.getCustomerId()).stream().map(OrderGrpcService::reply).toList())
                .build());
    }

    @Override
    public void cancelOrder(OrderId request, StreamObserver<OrderReply> observer) {
        GrpcErrors.respond(observer, () -> reply(orders.cancel(GrpcErrors.orderId(request.getId()))));
    }

    private static OrderReply reply(OrderResponse order) {
        return OrderReply.newBuilder().setId(order.id().toString())
                .setCustomerId(order.customerId() == null ? "" : order.customerId())
                .setSku(order.sku()).setQuantity(order.quantity())
                .setTotalAmount(GrpcErrors.money(order.totalAmount()))
                .setStatus(order.status()).build();
    }
}
