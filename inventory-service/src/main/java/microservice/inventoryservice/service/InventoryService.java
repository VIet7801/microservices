package microservice.inventoryservice.service;

import microservice.inventoryservice.model.Reservation;
import microservice.inventoryservice.dto.StockRequest;
import microservice.inventoryservice.dto.StockResponse;
import microservice.inventoryservice.repository.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class InventoryService {
    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    public java.util.List<java.util.Map<String, Object>> stocks() {
        return repository.stocks();
    }

    @Transactional
    public StockResponse reserve(StockRequest request) {
        if (request.orderId() == null || request.sku() == null || request.sku().isBlank()
                || request.sku().length() > 100 || request.quantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock request");
        }
        repository.lock(request.orderId());
        Reservation existing = repository.find(request.orderId());
        if (existing != null) {
            if (existing.sku() != null && (!existing.sku().equals(request.sku()) || existing.quantity() != request.quantity())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already has another reservation");
            }
            return toResponse(existing);
        }
        String status = repository.decrease(request.sku(), request.quantity()) ? "RESERVED" : "REJECTED";
        Reservation result = new Reservation(request.orderId(), request.sku(), request.quantity(), status);
        repository.save(result);
        return toResponse(result);
    }

    @Transactional
    public StockResponse release(UUID orderId) {
        repository.lock(orderId);
        Reservation existing = repository.find(orderId);
        if (existing != null && existing.status().equals("RELEASED")) {
            return toResponse(existing);
        }
        if (existing != null && existing.status().equals("RESERVED")) {
            repository.increase(existing.sku(), existing.quantity());
        }
        Reservation result = new Reservation(orderId, existing == null ? null : existing.sku(),
                existing == null ? 0 : existing.quantity(), "RELEASED");
        repository.save(result);
        return toResponse(result);
    }
    private StockResponse toResponse(Reservation reservation) {
        return new StockResponse(reservation.orderId(), reservation.sku(), reservation.quantity(), reservation.status());
    }
}

