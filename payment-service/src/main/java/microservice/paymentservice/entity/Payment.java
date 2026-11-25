package microservice.paymentservice.entity;

import jakarta.persistence.*;
import microservice.paymentservice.enums.PaymentStatus;
import org.springframework.data.domain.Persistable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class Payment implements Persistable<UUID> {
    @Id
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Transient
    private boolean isNew = true;

    protected Payment() {}

    public Payment(UUID orderId, PaymentStatus status, BigDecimal amount) {
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void refund() {
        if (status == PaymentStatus.SUCCEEDED) {
            status = PaymentStatus.REFUNDED;
        }
    }

    @Override
    public UUID getId() {
        return orderId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        isNew = false;
    }
}
