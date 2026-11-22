package microservice.paymentservice.entity;

import jakarta.persistence.*;
import microservice.paymentservice.enums.PaymentStatus;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class Payment {
    @Id
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    protected Payment() {}

    public Payment(UUID orderId, PaymentStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
