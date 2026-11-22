package microservice.orderservice;

import microservice.grpc.support.GrpcServerConfig;
import microservice.messaging.config.KafkaConfig;
import microservice.messaging.config.OutboxConfig;
import org.springframework.context.annotation.Import;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Import({KafkaConfig.class, OutboxConfig.class, GrpcServerConfig.class})
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
