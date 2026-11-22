package microservice.inventoryservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock")
public class Stock {
    @Id
    @Column(length = 100)
    private String sku;

    @Column(nullable = false)
    private int available;

    protected Stock() {}

    public String getSku() {
        return sku;
    }

    public int getAvailable() {
        return available;
    }

    public boolean take(int quantity) {
        if (available < quantity) {
            return false;
        }
        available -= quantity;
        return true;
    }

    public void giveBack(int quantity) {
        available += quantity;
    }
}
