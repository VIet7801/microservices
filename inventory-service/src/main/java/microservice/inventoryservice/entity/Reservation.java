package microservice.inventoryservice.entity;

import jakarta.persistence.*;
import microservice.inventoryservice.enums.ReservationStatus;
import java.util.UUID;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    private UUID orderId;

    @Column(length = 100)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    protected Reservation() {}

    public Reservation(UUID orderId, String sku, int quantity, ReservationStatus status) {
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
        this.status = status;
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
}
