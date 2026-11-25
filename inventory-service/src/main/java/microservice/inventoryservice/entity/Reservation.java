package microservice.inventoryservice.entity;

import jakarta.persistence.*;
import microservice.inventoryservice.enums.ReservationStatus;
import org.springframework.data.domain.Persistable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "reservation")
public class Reservation implements Persistable<UUID> {
    @Id
    private UUID orderId;

    @Column(length = 100)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Transient
    private boolean isNew = true;

    protected Reservation() {}

    public Reservation(UUID orderId, String sku, int quantity, ReservationStatus status, BigDecimal amount) {
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
        this.status = status;
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void release() {
        status = ReservationStatus.RELEASED;
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
