package microservice.grpc.support;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrpcServer implements AutoCloseable {
    private final Server server;

    public GrpcServer(int port, List<BindableService> services) {
        var builder = NettyServerBuilder.forPort(port);
        services.forEach(builder::addService);
        server = builder.build();
    }

    public void start() throws IOException {
        server.start();
        LoggerFactory.getLogger(GrpcServer.class).info("gRPC listening on {}", server.getPort());
    }

    @Override
    public void close() {
        server.shutdown();
        try {
            if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException e) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
