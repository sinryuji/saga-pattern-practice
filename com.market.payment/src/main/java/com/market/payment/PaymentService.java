package com.market.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${message.queue.err.product}")
    private String productErrQueue;

    private final RabbitTemplate rabbitTemplate;

    public void createPayment(DeliveryMessage deliveryMessage) {
        Payment payment = Payment.builder()
            .paymentId(deliveryMessage.getPaymentId())
            .userId(deliveryMessage.getUserId())
            .payAmount(deliveryMessage.getPayAmount())
            .payStatus("SUCCESS")
            .build();

        Integer payAmount = deliveryMessage.getPayAmount();
        if (payAmount >= 10000) {
            log.error("Payment amount exceeds limit {}", payAmount);
            deliveryMessage.setErrorType("PAYMENT_LIMIT_EXCEED");
            this.rollbackPayment(deliveryMessage);
        }
    }

    public void rollbackPayment(DeliveryMessage deliveryMessage) {
        log.info("Rollback payment message: {}", deliveryMessage);
        rabbitTemplate.convertAndSend(productErrQueue, deliveryMessage);
    }
}
