package microservice.inventoryservice.messaging;

import microservice.inventoryservice.service.InventoryService;

import com.fasterxml.jackson.databind.ObjectMapper;
import microservice.inventoryservice.dto.StockRequest;
import microservice.messaging.outbox.Outbox;
import microservice.messaging.dto.SagaMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryListener {
    private final InventoryService service;
    private final Outbox outbox;
    private final ObjectMapper mapper;

    public InventoryListener(InventoryService service, Outbox outbox, ObjectMapper mapper) {
        this.service = service;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "inventory-commands")
    @Transactional
    public void receive(String payload) throws Exception {
        SagaMessage message = mapper.readValue(payload, SagaMessage.class);
        if (message.type().equals("ReserveStock")) {
            var result = service.reserve(new StockRequest(message.orderId(), message.sku(), message.quantity()));
            String type = result.status().equals("RESERVED") ? "StockReserved" : "StockRejected";
            outbox.add("order-results", message.reply(type));
        } else if (message.type().equals("ReleaseStock")) {
            service.release(message.orderId());
            outbox.add("order-results", message.reply("StockReleased"));
        } else {
            throw new IllegalArgumentException("Unknown inventory command: " + message.type());
        }
    }
}

