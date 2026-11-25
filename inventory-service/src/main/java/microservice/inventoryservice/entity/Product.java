package microservice.inventoryservice.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
public class Product implements Persistable<String> {
    @Id
    @Column(length = 100)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int available;

    @Column(nullable = false)
    private boolean active;

    @Transient
    private boolean isNew = true;

    protected Product() {}

    public Product(String sku, String name, BigDecimal price, int available) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.available = available;
        this.active = true;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getAvailable() {
        return available;
    }

    public boolean isActive() {
        return active;
    }

    public void update(String name, BigDecimal price, int available) {
        this.name = name;
        this.price = price;
        this.available = available;
    }

    public void deactivate() {
        active = false;
    }

    public boolean take(int quantity) {
        if (!active || available < quantity) {
            return false;
        }
        available -= quantity;
        return true;
    }

    public void giveBack(int quantity) {
        available += quantity;
    }

    @Override
    public String getId() {
        return sku;
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
