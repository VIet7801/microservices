package microservice.orderservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import microservice.messaging.dto.SagaMessage;
import microservice.orderservice.service.OrderSaga;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderResultListener {
    private final OrderSaga saga;
    private final ObjectMapper mapper;

    public OrderResultListener(OrderSaga saga, ObjectMapper mapper) {
        this.saga = saga;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "order-results")
    public void receive(String payload) throws JsonProcessingException {
        saga.handle(mapper.readValue(payload, SagaMessage.class));
    }
}
