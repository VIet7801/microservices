package microservice.messaging.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {
    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafka) {
        var recoverer = new DeadLetterPublishingRecoverer(kafka,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
        recoverer.setFailIfSendResultIsError(true);
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000, FixedBackOff.UNLIMITED_ATTEMPTS));
        handler.addNotRetryableExceptions(IllegalArgumentException.class, JsonProcessingException.class);
        return handler;
    }

    @Bean
    NewTopic inventoryCommands() {
        return TopicBuilder.name("inventory-commands").partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic paymentCommands() {
        return TopicBuilder.name("payment-commands").partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic orderResults() {
        return TopicBuilder.name("order-results").partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic inventoryDeadLetters() {
        return TopicBuilder.name("inventory-commands.DLT").partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic paymentDeadLetters() {
        return TopicBuilder.name("payment-commands.DLT").partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic resultDeadLetters() {
        return TopicBuilder.name("order-results.DLT").partitions(1).replicas(1).build();
    }
}
