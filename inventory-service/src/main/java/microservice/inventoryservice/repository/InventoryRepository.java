package microservice.inventoryservice.repository;

import microservice.inventoryservice.model.Reservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class InventoryRepository {
    private final JdbcTemplate jdbc;

    public InventoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void lock(UUID orderId) {
        jdbc.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", orderId.toString());
    }

    public Reservation find(UUID orderId) {
        var rows = jdbc.query("SELECT * FROM reservation WHERE order_id = ?",
                (rs, n) -> new Reservation(rs.getObject("order_id", UUID.class),
                        rs.getString("sku"), rs.getInt("quantity"), rs.getString("status")), orderId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    public boolean decrease(String sku, int quantity) {
        return jdbc.update("UPDATE stock SET available = available - ? WHERE sku = ? AND available >= ?",
                quantity, sku, quantity) == 1;
    }

    public void increase(String sku, int quantity) {
        jdbc.update("UPDATE stock SET available = available + ? WHERE sku = ?", quantity, sku);
    }

    public void save(Reservation result) {
        jdbc.update("""
                INSERT INTO reservation(order_id, sku, quantity, status) VALUES (?, ?, ?, ?)
                ON CONFLICT (order_id) DO UPDATE SET status = EXCLUDED.status
                """, result.orderId(), result.sku(), result.quantity(), result.status());
    }

    public List<Map<String, Object>> stocks() {
        return jdbc.queryForList("SELECT sku, available FROM stock ORDER BY sku");
    }
}

