package microservice.messaging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import microservice.messaging.outbox.Outbox;
import microservice.messaging.outbox.OutboxPublisher;
import microservice.messaging.outbox.ProcessedMessages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class OutboxConfig {
    @Bean
    ProcessedMessages processedMessages(JdbcTemplate jdbc) {
        return new ProcessedMessages(jdbc);
    }

    @Bean
    Outbox outbox(JdbcTemplate jdbc, ObjectMapper mapper) {
        return new Outbox(jdbc, mapper);
    }

    @Bean
    OutboxPublisher outboxPublisher(JdbcTemplate jdbc, KafkaTemplate<String, String> kafka) {
        return new OutboxPublisher(jdbc, kafka);
    }
}
