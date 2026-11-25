package microservice.inventoryservice.repository;

import jakarta.persistence.LockModeType;
import microservice.inventoryservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.sku = :sku")
    Optional<Product> lockBySku(String sku);

    List<Product> findByActiveTrueOrderBySku();
}
