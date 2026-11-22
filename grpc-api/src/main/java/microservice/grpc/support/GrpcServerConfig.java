package microservice.grpc.support;

import io.grpc.BindableService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GrpcServerConfig {
    @Bean(initMethod = "start", destroyMethod = "close")
    GrpcServer grpcServer(@Value("${grpc.server.port}") int port, List<BindableService> services) {
        return new GrpcServer(port, services);
    }
}
