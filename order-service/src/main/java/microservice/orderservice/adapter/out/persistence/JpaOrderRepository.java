package microservice.orderservice.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import microservice.orderservice.application.port.out.OrderRepository;
import microservice.orderservice.domain.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaOrderRepository implements OrderRepository {

    private final EntityManager entityManager;
    private final OrderJpaRepository jpaRepository;

    public JpaOrderRepository(
            EntityManager entityManager,
            OrderJpaRepository jpaRepository
    ) {
        this.entityManager = entityManager;
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void insert(Order order) {
        entityManager.persist(new OrderEntity(order));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id).map(OrderEntity::toDomain);
    }
}