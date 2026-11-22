package microservice.inventoryservice.repository;

import jakarta.persistence.LockModeType;
import microservice.inventoryservice.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.sku = :sku")
    Optional<Stock> lockBySku(String sku);
}
