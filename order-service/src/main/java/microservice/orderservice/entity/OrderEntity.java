package microservice.orderservice.entity;

import jakarta.persistence.*;
import microservice.orderservice.enums.OrderStatus;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String customerId;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    protected OrderEntity() {}

    public static OrderEntity pending(String customerId, String sku, int quantity) {
        if (customerId == null || customerId.isBlank() || customerId.length() > 100) {
            throw new IllegalArgumentException("Customer ID must contain 1 to 100 characters");
        }
        if (sku == null || sku.isBlank() || sku.length() > 100) {
            throw new IllegalArgumentException("SKU must contain 1 to 100 characters");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        OrderEntity order = new OrderEntity();
        order.customerId = customerId;
        order.sku = sku;
        order.quantity = quantity;
        order.status = OrderStatus.PENDING;
        return order;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }
}
