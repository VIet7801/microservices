package microservice.inventoryservice.repository;

import jakarta.persistence.LockModeType;
import microservice.inventoryservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.orderId = :orderId")
    Optional<Reservation> lockByOrderId(UUID orderId);
}
