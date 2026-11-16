package microservice.orderservice.config;

import microservice.orderservice.application.OrderService;
import microservice.orderservice.application.port.in.OrderUseCase;
import microservice.orderservice.application.port.out.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfiguration {

    @Bean
    OrderUseCase orderUseCase(OrderRepository repository) {
        return new OrderService(repository);
    }
}