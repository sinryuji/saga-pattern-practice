package com.market.order;

import com.market.order.OrderEndpoint.OrderRequestDto;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${message.queue.product}")
    private String productQueue;

    private final Map<UUID, Order> orderStore = new HashMap<>();

    public Order createOrder(OrderRequestDto orderRequestDto) {
        Order order = orderRequestDto.toOrder();
        orderStore.put(order.getOrderId(), order);

        DeliveryMessage deliveryMessage = orderRequestDto.toDeliveryMessage(order.getOrderId());

        rabbitTemplate.convertAndSend(productQueue, deliveryMessage);

        return order;
    }

    public Order getOrder(UUID orderId) {
        return orderStore.get(orderId);
    }
}
