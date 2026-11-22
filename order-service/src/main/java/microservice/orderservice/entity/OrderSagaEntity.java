package microservice.orderservice.entity;

import jakarta.persistence.*;
import microservice.orderservice.enums.SagaStep;
import java.util.UUID;

@Entity
@Table(name = "order_saga")
public class OrderSagaEntity {
    @Id
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SagaStep step;

    @Column(nullable = false)
    private boolean paymentDeclined;

    protected OrderSagaEntity() {}

    public static OrderSagaEntity start(UUID orderId, boolean paymentDeclined) {
        OrderSagaEntity saga = new OrderSagaEntity();
        saga.orderId = orderId;
        saga.step = SagaStep.RESERVING;
        saga.paymentDeclined = paymentDeclined;
        return saga;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public SagaStep getStep() {
        return step;
    }

    public boolean isPaymentDeclined() {
        return paymentDeclined;
    }

    public void moveTo(SagaStep step) {
        this.step = step;
    }
}
