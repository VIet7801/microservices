package microservice.inventoryservice.service;

import microservice.common.exception.BusinessConflictException;
import microservice.inventoryservice.dto.StockItemResponse;
import microservice.inventoryservice.dto.StockRequest;
import microservice.inventoryservice.dto.StockResponse;
import microservice.inventoryservice.entity.Reservation;
import microservice.inventoryservice.entity.Stock;
import microservice.inventoryservice.enums.ReservationStatus;
import microservice.inventoryservice.repository.ReservationRepository;
import microservice.inventoryservice.repository.StockRepository;
import microservice.messaging.dto.SagaMessage;
import microservice.messaging.outbox.Outbox;
import microservice.messaging.outbox.ProcessedMessages;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InventoryService {
    private final StockRepository stocks;
    private final ReservationRepository reservations;
    private final Outbox outbox;
    private final ProcessedMessages processed;

    public InventoryService(StockRepository stocks, ReservationRepository reservations,
                            Outbox outbox, ProcessedMessages processed) {
        this.stocks = stocks;
        this.reservations = reservations;
        this.outbox = outbox;
        this.processed = processed;
    }

    public List<StockItemResponse> stocks() {
        return stocks.findAll(Sort.by("sku")).stream()
                .map(stock -> new StockItemResponse(stock.getSku(), stock.getAvailable())).toList();
    }

    public StockResponse find(UUID orderId) {
        return reservations.findById(orderId).map(InventoryService::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found"));
    }

    @Transactional
    public StockResponse reserve(StockRequest request) {
        if (request.orderId() == null || request.sku() == null || request.sku().isBlank()
                || request.sku().length() > 100 || request.quantity() < 1) {
            throw new IllegalArgumentException("Invalid stock request");
        }
        Optional<Stock> stock = stocks.lockBySku(request.sku());
        Optional<Reservation> existing = reservations.findById(request.orderId());
        if (existing.isPresent()) {
            Reservation reservation = existing.get();
            if (reservation.getSku() != null && (!reservation.getSku().equals(request.sku())
                    || reservation.getQuantity() != request.quantity())) {
                throw new BusinessConflictException("Order already has another reservation");
            }
            return toResponse(reservation);
        }
        boolean taken = stock.isPresent() && stock.get().take(request.quantity());
        ReservationStatus status = taken ? ReservationStatus.RESERVED : ReservationStatus.REJECTED;
        return toResponse(reservations.save(new Reservation(request.orderId(), request.sku(), request.quantity(), status)));
    }

    @Transactional
    public StockResponse release(UUID orderId) {
        Optional<Reservation> existing = reservations.lockByOrderId(orderId);
        if (existing.isEmpty()) {
            return toResponse(reservations.save(new Reservation(orderId, null, 0, ReservationStatus.RELEASED)));
        }
        Reservation reservation = existing.get();
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            stocks.lockBySku(reservation.getSku()).ifPresent(stock -> stock.giveBack(reservation.getQuantity()));
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
            String type = result.status().equals(ReservationStatus.RESERVED.name()) ? "StockReserved" : "StockRejected";
            outbox.add("order-results", message.reply(type));
        } else if (message.type().equals("ReleaseStock")) {
            release(message.orderId());
            outbox.add("order-results", message.reply("StockReleased"));
        } else {
            throw new IllegalArgumentException("Unknown inventory command: " + message.type());
        }
    }

    private static StockResponse toResponse(Reservation reservation) {
        return new StockResponse(reservation.getOrderId(), reservation.getSku(),
                reservation.getQuantity(), reservation.getStatus().name());
    }
}
