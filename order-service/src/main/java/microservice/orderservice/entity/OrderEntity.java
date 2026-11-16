package microservice.orderservice.entity;

import jakarta.persistence.*;
import microservice.orderservice.enums.OrderStatus;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    //Stock Keeping Unit
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    protected OrderEntity() {}

    public static OrderEntity pending(String sku, int quantity) {
        if (sku == null || sku.isBlank() || sku.length() > 100) {
            throw new IllegalArgumentException("SKU must contain 1 to 100 characters");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        OrderEntity order = new OrderEntity();
        order.sku = sku;
        order.quantity = quantity;
        order.status = OrderStatus.PENDING;
        return order;
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
