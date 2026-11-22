package microservice.orderservice.service;

import microservice.messaging.dto.SagaMessage;
import microservice.messaging.outbox.Outbox;
import microservice.messaging.outbox.ProcessedMessages;
import microservice.orderservice.entity.OrderEntity;
import microservice.orderservice.entity.OrderSagaEntity;
import microservice.orderservice.enums.OrderStatus;
import microservice.orderservice.enums.SagaStep;
import microservice.orderservice.repository.OrderRepository;
import microservice.orderservice.repository.SagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
public class OrderSaga {
    private static final Set<String> RESULTS = Set.of(
            "StockReserved", "StockRejected", "PaymentSucceeded", "PaymentDeclined", "StockReleased");

    private final OrderRepository orders;
    private final SagaRepository sagas;
    private final Outbox outbox;
    private final ProcessedMessages processed;

    public OrderSaga(OrderRepository orders, SagaRepository sagas, Outbox outbox, ProcessedMessages processed) {
        this.orders = orders;
        this.sagas = sagas;
        this.outbox = outbox;
        this.processed = processed;
    }

    @Transactional
    public void handle(SagaMessage message) {
        if (!RESULTS.contains(message.type())) {
            throw new IllegalArgumentException("Unknown order result: " + message.type());
        }
        if (!processed.firstTime(message.messageId())) {
            return;
        }
        OrderSagaEntity saga = sagas.lockByOrderId(message.orderId()).orElse(null);
        if (saga == null) {
            return;
        }
        OrderEntity order = orders.findById(message.orderId()).orElseThrow();
        SagaStep step = saga.getStep();
        String type = message.type();

        if (step == SagaStep.RESERVING && type.equals("StockReserved")) {
            saga.moveTo(SagaStep.PAYING);
            outbox.add("payment-commands", SagaMessage.of(order.getId(), "ChargePayment",
                    order.getSku(), order.getQuantity(), saga.isPaymentDeclined()));
        } else if (step == SagaStep.RESERVING && type.equals("StockRejected")) {
            order.changeStatus(OrderStatus.CANCELED);
            saga.moveTo(SagaStep.DONE);
        } else if (step == SagaStep.PAYING && type.equals("PaymentSucceeded")) {
            order.changeStatus(OrderStatus.COMPLETED);
            saga.moveTo(SagaStep.DONE);
        } else if (step == SagaStep.PAYING && type.equals("PaymentDeclined")) {
            saga.moveTo(SagaStep.RELEASING);
            outbox.add("inventory-commands", SagaMessage.of(order.getId(), "ReleaseStock",
                    order.getSku(), order.getQuantity(), saga.isPaymentDeclined()));
        } else if (step == SagaStep.RELEASING && type.equals("StockReleased")) {
            order.changeStatus(OrderStatus.CANCELED);
            saga.moveTo(SagaStep.DONE);
        }
    }
}
