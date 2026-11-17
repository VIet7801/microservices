package microservice.paymentservice;

import microservice.messaging.config.KafkaConfig;
import microservice.messaging.config.OutboxConfig;
import org.springframework.context.annotation.Import;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Import({KafkaConfig.class, OutboxConfig.class})
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

}


