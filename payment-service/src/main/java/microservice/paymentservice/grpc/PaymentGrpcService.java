package microservice.paymentservice.grpc;

import io.grpc.stub.StreamObserver;
import microservice.grpc.api.*;
import microservice.grpc.support.GrpcErrors;
import microservice.paymentservice.dto.PaymentRequest;
import microservice.paymentservice.dto.PaymentResponse;
import microservice.paymentservice.service.PaymentService;
import org.springframework.stereotype.Component;

@Component
public class PaymentGrpcService extends PaymentApiGrpc.PaymentApiImplBase {
    private final PaymentService payments;

    public PaymentGrpcService(PaymentService payments) {
        this.payments = payments;
    }

    @Override
    public void pay(PayInput request, StreamObserver<PaymentReply> observer) {
        GrpcErrors.respond(observer, () -> reply(payments.pay(new PaymentRequest(
                GrpcErrors.orderId(request.getOrderId()), request.getDeclined(),
                GrpcErrors.money(request.getAmount())))));
    }

    @Override
    public void getPayment(OrderId request, StreamObserver<PaymentReply> observer) {
        GrpcErrors.respond(observer, () -> reply(payments.find(GrpcErrors.orderId(request.getId()))));
    }

    private PaymentReply reply(PaymentResponse payment) {
        return PaymentReply.newBuilder().setOrderId(payment.orderId().toString())
                .setStatus(payment.status()).setAmount(GrpcErrors.money(payment.amount())).build();
    }
}
