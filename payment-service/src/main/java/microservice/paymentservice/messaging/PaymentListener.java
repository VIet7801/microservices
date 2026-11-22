package microservice.paymentservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import microservice.messaging.dto.SagaMessage;
import microservice.paymentservice.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentListener {
    private final PaymentService service;
    private final ObjectMapper mapper;

    public PaymentListener(PaymentService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "payment-commands")
    public void receive(String payload) throws JsonProcessingException {
        service.handle(mapper.readValue(payload, SagaMessage.class));
    }
}
