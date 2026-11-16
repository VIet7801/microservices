package microservice.orderservice.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity implements Persistable<UUID> {
    public enum Status { PENDING }

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Transient
    private boolean newEntity = true;

    protected OrderEntity() {}

    public static OrderEntity pending(String sku, int quantity) {
        if (sku == null || sku.isBlank() || sku.length() > 100) {
            throw new IllegalArgumentException("SKU must contain 1 to 100 characters");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        OrderEntity order = new OrderEntity();
        order.id = UUID.randomUUID();
        order.sku = sku;
        order.quantity = quantity;
        order.status = Status.PENDING;
        return order;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        // Assigned UUIDs cannot indicate whether this entity has been persisted.
        return newEntity;
    }

    @PostPersist
    @PostLoad
    private void markPersisted() {
        newEntity = false;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public Status getStatus() {
        return status;
    }
}
