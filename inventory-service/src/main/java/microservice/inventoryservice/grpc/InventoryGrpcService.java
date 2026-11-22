package microservice.inventoryservice.grpc;

import io.grpc.stub.StreamObserver;
import microservice.grpc.api.*;
import microservice.grpc.support.GrpcErrors;
import microservice.inventoryservice.dto.StockRequest;
import microservice.inventoryservice.dto.StockResponse;
import microservice.inventoryservice.service.InventoryService;
import org.springframework.stereotype.Component;

@Component
public class InventoryGrpcService extends InventoryApiGrpc.InventoryApiImplBase {
    private final InventoryService inventory;

    public InventoryGrpcService(InventoryService inventory) {
        this.inventory = inventory;
    }

    @Override
    public void listStock(Empty request, StreamObserver<StockList> observer) {
        GrpcErrors.respond(observer, () -> StockList.newBuilder().addAllStocks(inventory.stocks().stream()
                .map(stock -> StockItem.newBuilder().setSku(stock.sku())
                        .setAvailable(stock.available()).build()).toList()).build());
    }

    @Override
    public void getReservation(OrderId request, StreamObserver<ReservationReply> observer) {
        GrpcErrors.respond(observer, () -> reply(inventory.find(GrpcErrors.orderId(request.getId()))));
    }

    @Override
    public void reserveStock(ReserveStockInput request, StreamObserver<ReservationReply> observer) {
        GrpcErrors.respond(observer, () -> reply(inventory.reserve(new StockRequest(
                GrpcErrors.orderId(request.getOrderId()), request.getSku(), request.getQuantity()))));
    }

    @Override
    public void releaseStock(OrderId request, StreamObserver<ReservationReply> observer) {
        GrpcErrors.respond(observer, () -> reply(inventory.release(GrpcErrors.orderId(request.getId()))));
    }

    private ReservationReply reply(StockResponse stock) {
        return ReservationReply.newBuilder().setOrderId(stock.orderId().toString())
                .setSku(stock.sku() == null ? "" : stock.sku())
                .setQuantity(stock.quantity()).setStatus(stock.status()).build();
    }
}
