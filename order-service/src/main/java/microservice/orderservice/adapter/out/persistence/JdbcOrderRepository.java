package microservice.orderservice.adapter.out.persistence;

import microservice.orderservice.application.port.out.OrderRepository;
import microservice.orderservice.domain.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcOrderRepository implements OrderRepository {
    private final JdbcTemplate jdbc;

    public JdbcOrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(Order order) {
        jdbc.update("""
                INSERT INTO orders (id, sku, quantity, status)
                VALUES (?, ?, ?, ?)
                """, order.id(), order.sku(), order.quantity(), order.status().name());
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jdbc.query("""
                SELECT id, sku, quantity, status FROM orders WHERE id = ?
                """, (rs, rowNum) -> new Order(
                rs.getObject("id", UUID.class),
                rs.getString("sku"),
                rs.getInt("quantity"),
                Order.Status.valueOf(rs.getString("status"))
        ), id).stream().findFirst();
    }
}