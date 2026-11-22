package microservice.inventoryservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import microservice.inventoryservice.service.InventoryService;
import microservice.messaging.dto.SagaMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryListener {
    private final InventoryService service;
    private final ObjectMapper mapper;

    public InventoryListener(InventoryService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "inventory-commands")
    public void receive(String payload) throws JsonProcessingException {
        service.handle(mapper.readValue(payload, SagaMessage.class));
    }
}
