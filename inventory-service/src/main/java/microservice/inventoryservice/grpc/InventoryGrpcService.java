package microservice.inventoryservice.grpc;

import io.grpc.stub.StreamObserver;
import microservice.grpc.api.*;
import microservice.grpc.support.GrpcErrors;
import microservice.inventoryservice.dto.ProductRequest;
import microservice.inventoryservice.dto.ProductResponse;
import microservice.inventoryservice.dto.StockRequest;
import microservice.inventoryservice.dto.StockResponse;
import microservice.inventoryservice.service.InventoryService;
import microservice.inventoryservice.service.ProductService;
import org.springframework.stereotype.Component;

@Component
public class InventoryGrpcService extends InventoryApiGrpc.InventoryApiImplBase {
    private final InventoryService inventory;
    private final ProductService products;

    public InventoryGrpcService(InventoryService inventory, ProductService products) {
        this.inventory = inventory;
        this.products = products;
    }

    @Override
    public void createProduct(ProductInput request, StreamObserver<ProductReply> observer) {
        GrpcErrors.respond(observer, () -> reply(products.create(toRequest(request))));
    }

    @Override
    public void getProduct(Sku request, StreamObserver<ProductReply> observer) {
        GrpcErrors.respond(observer, () -> reply(products.find(request.getSku())));
    }

    @Override
    public void listProducts(Empty request, StreamObserver<ProductList> observer) {
        GrpcErrors.respond(observer, () -> ProductList.newBuilder()
                .addAllProducts(products.list().stream().map(InventoryGrpcService::reply).toList()).build());
    }

    @Override
    public void updateProduct(ProductInput request, StreamObserver<ProductReply> observer) {
        GrpcErrors.respond(observer, () -> reply(products.update(toRequest(request))));
    }

    @Override
    public void deleteProduct(Sku request, StreamObserver<Empty> observer) {
        GrpcErrors.respond(observer, () -> {
            products.delete(request.getSku());
            return Empty.getDefaultInstance();
        });
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

    private static ProductRequest toRequest(ProductInput input) {
        return new ProductRequest(input.getSku(), input.getName(),
                GrpcErrors.money(input.getPrice()), input.getAvailable());
    }

    private static ProductReply reply(ProductResponse product) {
        return ProductReply.newBuilder().setSku(product.sku()).setName(product.name())
                .setPrice(GrpcErrors.money(product.price())).setAvailable(product.available())
                .setActive(product.active()).build();
    }

    private static ReservationReply reply(StockResponse stock) {
        return ReservationReply.newBuilder().setOrderId(stock.orderId().toString())
                .setSku(stock.sku() == null ? "" : stock.sku())
                .setQuantity(stock.quantity()).setStatus(stock.status())
                .setAmount(GrpcErrors.money(stock.amount())).build();
    }
}
