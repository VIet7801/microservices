package microservice.inventoryservice.controller;

import microservice.inventoryservice.dto.StockRequest;
import microservice.inventoryservice.dto.StockResponse;
import microservice.inventoryservice.service.InventoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> stocks() {
        return service.stocks();
    }

    @PostMapping("/reserve")
    public StockResponse reserve(@RequestBody StockRequest request) {
        return service.reserve(request);
    }

    @PostMapping("/release/{orderId}")
    public StockResponse release(@PathVariable UUID orderId) {
        return service.release(orderId);
    }
}
