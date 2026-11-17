package microservice.paymentservice.service;

import microservice.paymentservice.dto.PaymentRequest;
import microservice.paymentservice.dto.PaymentResponse;
import microservice.paymentservice.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {
    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        if (request.orderId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderId is required");
        }
        var payment = repository.pay(request.orderId(), request.declined());
        return new PaymentResponse(payment.orderId(), payment.status());
    }
}

