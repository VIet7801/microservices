package microservice.messaging.outbox;

import microservice.messaging.dto.SagaMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;

public class Outbox {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public Outbox(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public void add(String topic, SagaMessage message) {
        try {
            jdbc.update("INSERT INTO outbox(id, topic, message_key, payload) VALUES (?, ?, ?, ?)",
                    message.messageId(), topic, message.orderId().toString(), mapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize saga message", e);
        }
    }
}
