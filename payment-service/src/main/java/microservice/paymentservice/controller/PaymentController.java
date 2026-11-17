package microservice.paymentservice.controller;

import microservice.paymentservice.dto.PaymentRequest;
import microservice.paymentservice.dto.PaymentResponse;
import microservice.paymentservice.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public PaymentResponse pay(@RequestBody PaymentRequest request) {
        return service.pay(request);
    }
}
