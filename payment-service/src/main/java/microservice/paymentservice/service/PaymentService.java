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
import java.math.BigDecimal;
import microservice.common.exception.InvalidStateException;
import microservice.common.exception.NotFoundException;
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
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }

    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        if (request.orderId() == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        if (request.amount() != null && request.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be zero or more");
        }
        Payment payment = repository.findById(request.orderId()).orElseGet(() -> {
            PaymentStatus status = request.declined() ? PaymentStatus.DECLINED : PaymentStatus.SUCCEEDED;
            return repository.save(new Payment(request.orderId(), status, request.amount()));
        });
        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse refund(UUID orderId) {
        Payment payment = repository.findById(orderId)
                .orElseThrow(() -> new InvalidStateException("Cannot refund missing payment for order " + orderId));
        payment.refund();
        return toResponse(payment);
    }

    @Transactional
    public void handle(SagaMessage message) {
        if (!message.type().equals("ChargePayment") && !message.type().equals("RefundPayment")) {
            throw new IllegalArgumentException("Unknown payment command: " + message.type());
        }
        if (!processed.firstTime(message.messageId())) {
            return;
        }
        if (message.type().equals("ChargePayment")) {
            PaymentResponse result = pay(new PaymentRequest(message.orderId(), message.paymentDeclined(), message.amount()));
            String type = result.status().equals(PaymentStatus.SUCCEEDED.name()) ? "PaymentSucceeded" : "PaymentDeclined";
            outbox.add("order-results", message.reply(type));
        } else {
            refund(message.orderId());
            outbox.add("order-results", message.reply("PaymentRefunded"));
        }
    }

    private static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getOrderId(), payment.getStatus().name(), payment.getAmount());
    }
}
