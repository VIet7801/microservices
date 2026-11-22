package microservice.paymentservice.service;

import microservice.messaging.dto.SagaMessage;
import microservice.messaging.outbox.Outbox;
import microservice.messaging.outbox.ProcessedMessages;
import microservice.paymentservice.dto.PaymentRequest;
import microservice.paymentservice.dto.PaymentResponse;
import microservice.paymentservice.entity.Payment;
import microservice.paymentservice.enums.PaymentStatus;
import microservice.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository repository;
    private final Outbox outbox;
    private final ProcessedMessages processed;

    public PaymentService(PaymentRepository repository, Outbox outbox, ProcessedMessages processed) {
        this.repository = repository;
        this.outbox = outbox;
        this.processed = processed;
    }

    public PaymentResponse find(UUID orderId) {
        return repository.findById(orderId).map(PaymentService::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Payment not found"));
    }

    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        if (request.orderId() == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        Payment payment = repository.findById(request.orderId()).orElseGet(() -> {
            PaymentStatus status = request.declined() ? PaymentStatus.DECLINED : PaymentStatus.SUCCEEDED;
            return repository.save(new Payment(request.orderId(), status));
        });
        return toResponse(payment);
    }

    @Transactional
    public void handle(SagaMessage message) {
        if (!message.type().equals("ChargePayment")) {
            throw new IllegalArgumentException("Unknown payment command: " + message.type());
        }
        if (!processed.firstTime(message.messageId())) {
            return;
        }
        PaymentResponse result = pay(new PaymentRequest(message.orderId(), message.paymentDeclined()));
        String type = result.status().equals(PaymentStatus.SUCCEEDED.name()) ? "PaymentSucceeded" : "PaymentDeclined";
        outbox.add("order-results", message.reply(type));
    }

    private static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getOrderId(), payment.getStatus().name());
    }
}
