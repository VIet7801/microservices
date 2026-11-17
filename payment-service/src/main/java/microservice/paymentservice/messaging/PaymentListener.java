package microservice.paymentservice.messaging;

import microservice.paymentservice.service.PaymentService;

import com.fasterxml.jackson.databind.ObjectMapper;
import microservice.paymentservice.dto.PaymentRequest;
import microservice.messaging.outbox.Outbox;
import microservice.messaging.dto.SagaMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentListener {
    private final PaymentService service;
    private final Outbox outbox;
    private final ObjectMapper mapper;

    public PaymentListener(PaymentService service, Outbox outbox, ObjectMapper mapper) {
        this.service = service;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "payment-commands")
    @Transactional
    public void receive(String payload) throws Exception {
        SagaMessage message = mapper.readValue(payload, SagaMessage.class);
        if (!message.type().equals("ChargePayment")) {
            throw new IllegalArgumentException("Unknown payment command: " + message.type());
        }
        var result = service.pay(new PaymentRequest(message.orderId(), message.paymentDeclined()));
        outbox.add("order-results", message.reply(result.status().equals("SUCCEEDED") ? "PaymentSucceeded" : "PaymentDeclined"));
    }
}

