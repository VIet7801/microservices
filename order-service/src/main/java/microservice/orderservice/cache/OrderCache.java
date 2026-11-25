package microservice.orderservice.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import microservice.orderservice.dto.OrderResponse;
import microservice.orderservice.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderCache {
    private static final Logger log = LoggerFactory.getLogger(OrderCache.class);
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public OrderCache(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    public Optional<OrderResponse> get(UUID id) {
        try {
            String json = redis.opsForValue().get("orders:" + id);
            return json == null ? Optional.empty() : Optional.of(mapper.readValue(json, OrderResponse.class));
        } catch (Exception e) {
            log.warn("Cannot read order cache: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void put(OrderResponse order) {
        if (!order.status().equals(OrderStatus.CANCELED.name())) {
            return;
        }
        try {
            redis.opsForValue().set("orders:" + order.id(), mapper.writeValueAsString(order), Duration.ofMinutes(10));
        } catch (Exception e) {
            log.warn("Cannot write order cache: {}", e.getMessage());
        }
    }
}
