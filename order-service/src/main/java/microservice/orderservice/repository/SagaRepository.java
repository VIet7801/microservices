package microservice.orderservice.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.UUID;

@Repository
public class SagaRepository {
    private final JdbcTemplate jdbc;

    public SagaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void start(UUID orderId, boolean declined) {
        jdbc.update("INSERT INTO order_saga(order_id, step, payment_declined) VALUES (?, 'RESERVING', ?)",
                orderId, declined);
    }

    public Map<String, Object> lock(UUID orderId) {
        var rows = jdbc.queryForList("SELECT * FROM order_saga WHERE order_id = ? FOR UPDATE", orderId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    public void move(UUID orderId, String step) {
        jdbc.update("UPDATE order_saga SET step = ? WHERE order_id = ?", step, orderId);
    }
}
