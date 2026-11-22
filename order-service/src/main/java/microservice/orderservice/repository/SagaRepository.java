package microservice.orderservice.repository;

import jakarta.persistence.LockModeType;
import microservice.orderservice.entity.OrderSagaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

public interface SagaRepository extends JpaRepository<OrderSagaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM OrderSagaEntity s WHERE s.orderId = :orderId")
    Optional<OrderSagaEntity> lockByOrderId(UUID orderId);
}
