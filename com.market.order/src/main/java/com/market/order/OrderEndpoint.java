package com.market.order;

import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderEndpoint {

    private final OrderService orderService;

    @RabbitListener(queues = "${message.queue.err.order}")
    public void receiveErrorMessage(DeliveryMessage deliveryMessage) {
        log.info("ERROR RECEIVED!!");
        orderService.rollbackOrder(deliveryMessage);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID orderId) {
        Order order = orderService.getOrder(orderId);

        return ResponseEntity.ok(order);
    }

    @PostMapping("/order")
    public ResponseEntity<Order> order(@RequestBody OrderRequestDto orderRequestDto) {
        Order order = orderService.createOrder(orderRequestDto);

        return ResponseEntity.ok(order);
    }

    @Data
    public static class OrderRequestDto {
        private String userId;
        private Integer productId;
        private Integer productQuantity;
        private Integer payAmount;

        public Order toOrder() {
            return Order.builder()
                .orderId(UUID.randomUUID())
                .userId(this.userId)
                .orderStatus("RECEIPT")
                .build();
        }

        public DeliveryMessage toDeliveryMessage(UUID orderId) {
            return DeliveryMessage.builder()
                .userId(this.userId)
                .orderId(orderId)
                .productId(this.productId)
                .productQuantity(this.productQuantity)
                .payAmount(this.payAmount)
                .build();
        }
    }
}
