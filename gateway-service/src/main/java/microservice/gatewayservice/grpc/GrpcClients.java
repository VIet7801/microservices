package microservice.gatewayservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import jakarta.annotation.PreDestroy;
import microservice.grpc.api.InventoryApiGrpc;
import microservice.grpc.api.OrderApiGrpc;
import microservice.grpc.api.PaymentApiGrpc;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GrpcClients {
    private final DiscoveryClient discovery;
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final AtomicInteger next = new AtomicInteger();

    public GrpcClients(DiscoveryClient discovery) {
        this.discovery = discovery;
    }

    public OrderApiGrpc.OrderApiBlockingStub orders() {
        return OrderApiGrpc.newBlockingStub(channel("order-service")).withDeadlineAfter(5, TimeUnit.SECONDS);
    }

    public InventoryApiGrpc.InventoryApiBlockingStub inventory() {
        return InventoryApiGrpc.newBlockingStub(channel("inventory-service")).withDeadlineAfter(5, TimeUnit.SECONDS);
    }

    public PaymentApiGrpc.PaymentApiBlockingStub payments() {
        return PaymentApiGrpc.newBlockingStub(channel("payment-service")).withDeadlineAfter(5, TimeUnit.SECONDS);
    }

    private ManagedChannel channel(String service) {
        var instances = discovery.getInstances(service);
        if (instances.isEmpty()) {
            throw Status.UNAVAILABLE.withDescription(service + " is unavailable").asRuntimeException();
        }
        var instance = instances.get(Math.floorMod(next.getAndIncrement(), instances.size()));
        String port = instance.getMetadata().get("grpc-port");
        if (port == null) {
            throw Status.UNAVAILABLE.withDescription(service + " has no gRPC port in Eureka").asRuntimeException();
        }
        String address = instance.getHost() + ":" + port;
        return channels.computeIfAbsent(address, key -> ManagedChannelBuilder
                .forAddress(instance.getHost(), Integer.parseInt(port)).usePlaintext().build());
    }

    @PreDestroy
    public void close() {
        channels.values().forEach(ManagedChannel::shutdownNow);
    }
}
