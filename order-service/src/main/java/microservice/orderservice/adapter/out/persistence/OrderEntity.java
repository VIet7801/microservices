package microservice.orderservice.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import microservice.orderservice.domain.Order;

import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, length = 30)
    private String status;

    protected OrderEntity() {
        // Constructor không tham số cho JPA.
    }

    OrderEntity(Order order) {
        this.id = order.id();
        this.sku = order.sku();
        this.quantity = order.quantity();
        this.status = order.status().name();
    }

    Order toDomain() {
        return new Order(id, sku, quantity, Order.Status.valueOf(status));
    }
}