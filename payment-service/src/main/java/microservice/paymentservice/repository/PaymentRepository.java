package microservice.paymentservice.repository;

import microservice.paymentservice.model.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbc;

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Payment pay(UUID orderId, boolean declined) {
        jdbc.update("INSERT INTO payment(order_id, status) VALUES (?, ?) ON CONFLICT (order_id) DO NOTHING",
                orderId, declined ? "DECLINED" : "SUCCEEDED");
        return jdbc.queryForObject("SELECT order_id, status FROM payment WHERE order_id = ?",
                (rs, n) -> new Payment(rs.getObject("order_id", UUID.class), rs.getString("status")), orderId);
    }
}

