package microservice.orderservice.repository;

import microservice.orderservice.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByCustomerId(String customerId);
}
