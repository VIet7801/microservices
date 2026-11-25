package microservice.inventoryservice.service;

import microservice.common.exception.BusinessConflictException;
import microservice.inventoryservice.dto.StockRequest;
import microservice.inventoryservice.dto.StockResponse;
import microservice.inventoryservice.entity.Product;
import microservice.inventoryservice.entity.Reservation;
import microservice.inventoryservice.enums.ReservationStatus;
import microservice.inventoryservice.repository.ProductRepository;
import microservice.inventoryservice.repository.ReservationRepository;
import microservice.messaging.dto.SagaMessage;
import microservice.messaging.outbox.Outbox;
import microservice.messaging.outbox.ProcessedMessages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import microservice.common.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InventoryService {
    private final ProductRepository products;
    private final ReservationRepository reservations;
    private final Outbox outbox;
    private final ProcessedMessages processed;

    public InventoryService(ProductRepository products, ReservationRepository reservations,
                            Outbox outbox, ProcessedMessages processed) {
        this.products = products;
        this.reservations = reservations;
        this.outbox = outbox;
        this.processed = processed;
    }

    public StockResponse find(UUID orderId) {
        return reservations.findById(orderId).map(InventoryService::toResponse)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));
    }

    @Transactional
    public StockResponse reserve(StockRequest request) {
        if (request.orderId() == null || request.sku() == null || request.sku().isBlank()
                || request.sku().length() > 100 || request.quantity() < 1) {
            throw new IllegalArgumentException("Invalid stock request");
        }
        Optional<Product> product = products.lockBySku(request.sku());
        Optional<Reservation> existing = reservations.findById(request.orderId());
        if (existing.isPresent()) {
            Reservation reservation = existing.get();
            if (reservation.getSku() != null && (!reservation.getSku().equals(request.sku())
                    || reservation.getQuantity() != request.quantity())) {
                throw new BusinessConflictException("Order already has another reservation");
            }
            return toResponse(reservation);
        }
        Reservation reservation;
        if (product.isPresent() && product.get().take(request.quantity())) {
            BigDecimal amount = product.get().getPrice().multiply(BigDecimal.valueOf(request.quantity()));
            reservation = new Reservation(request.orderId(), request.sku(), request.quantity(),
                    ReservationStatus.RESERVED, amount);
        } else {
            reservation = new Reservation(request.orderId(), request.sku(), request.quantity(),
                    ReservationStatus.REJECTED, null);
        }
        return toResponse(reservations.save(reservation));
    }

    @Transactional
    public StockResponse release(UUID orderId) {
        Optional<Reservation> existing = reservations.lockByOrderId(orderId);
        if (existing.isEmpty()) {
            return toResponse(reservations.save(new Reservation(orderId, null, 0, ReservationStatus.RELEASED, null)));
        }
        Reservation reservation = existing.get();
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            products.lockBySku(reservation.getSku()).ifPresent(product -> product.giveBack(reservation.getQuantity()));
        }
        reservation.release();
        return toResponse(reservation);
    }

    @Transactional
    public void handle(SagaMessage message) {
        if (!processed.firstTime(message.messageId())) {
            return;
        }
        if (message.type().equals("ReserveStock")) {
            StockResponse result = reserve(new StockRequest(message.orderId(), message.sku(), message.quantity()));
            if (result.status().equals(ReservationStatus.RESERVED.name())) {
                outbox.add("order-results", message.reply("StockReserved", result.amount()));
            } else {
                outbox.add("order-results", message.reply("StockRejected"));
            }
        } else if (message.type().equals("ReleaseStock")) {
            release(message.orderId());
            outbox.add("order-results", message.reply("StockReleased"));
        } else {
            throw new IllegalArgumentException("Unknown inventory command: " + message.type());
        }
    }

    private static StockResponse toResponse(Reservation reservation) {
        return new StockResponse(reservation.getOrderId(), reservation.getSku(),
                reservation.getQuantity(), reservation.getStatus().name(), reservation.getAmount());
    }
}
