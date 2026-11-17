package microservice.orderservice.service;

import microservice.messaging.outbox.Outbox;
import microservice.messaging.dto.SagaMessage;
import microservice.orderservice.enums.OrderStatus;
import microservice.orderservice.repository.OrderRepository;
import microservice.orderservice.repository.SagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderSaga {
    private final OrderRepository orders;
    private final SagaRepository sagas;
    private final Outbox outbox;

    public OrderSaga(OrderRepository orders, SagaRepository sagas, Outbox outbox) {
        this.orders = orders;
        this.sagas = sagas;
        this.outbox = outbox;
    }

    @Transactional
    public void handle(SagaMessage message) {
        var saga = sagas.lock(message.orderId());
        if (saga == null) {
            return;
        }
        var order = orders.findById(message.orderId()).orElseThrow();
        String step = (String) saga.get("step");
        boolean declined = (Boolean) saga.get("payment_declined");

        if (step.equals("RESERVING") && message.type().equals("StockReserved")) {
            sagas.move(order.getId(), "PAYING");
            outbox.add("payment-commands", SagaMessage.of(order.getId(), "ChargePayment",
                    order.getSku(), order.getQuantity(), declined));
        } else if (step.equals("RESERVING") && message.type().equals("StockRejected")) {
            order.changeStatus(OrderStatus.CANCELED);
            sagas.move(order.getId(), "DONE");
        } else if (step.equals("PAYING") && message.type().equals("PaymentSucceeded")) {
            order.changeStatus(OrderStatus.COMPLETED);
            sagas.move(order.getId(), "DONE");
        } else if (step.equals("PAYING") && message.type().equals("PaymentDeclined")) {
            sagas.move(order.getId(), "RELEASING");
            outbox.add("inventory-commands", SagaMessage.of(order.getId(), "ReleaseStock",
                    order.getSku(), order.getQuantity(), declined));
        } else if (step.equals("RELEASING") && message.type().equals("StockReleased")) {
            order.changeStatus(OrderStatus.CANCELED);
            sagas.move(order.getId(), "DONE");
        }
    }
}
