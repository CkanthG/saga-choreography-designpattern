package com.sree.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.order.dto.CustomerOrder;
import com.sree.order.dto.OrderEvent;
import com.sree.order.entity.Order;
import com.sree.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * To handle all order related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${kafka.topic.new-order:new-order}")
    private String kafkaTopic;

    /**
     * To create a new customer order.
     * @param customerOrder to create a new customer order.
     * @return order status.
     */
    public String createOrder(CustomerOrder customerOrder) {
        log.info("Create order :: {}", customerOrder);
        String status;
        Order order = new Order();
        order.setAmount(customerOrder.getAmount());
        order.setItem(customerOrder.getItem());
        order.setQuantity(customerOrder.getQuantity());
        try {
            order.setStatus("Order Created");
            Order savedOrder = orderRepository.save(order);
            customerOrder.setOrderId(savedOrder.getOrderId());
            OrderEvent orderEvent = new OrderEvent(customerOrder, "ORDER_CREATED");
            kafkaTemplate.send(kafkaTopic, orderEvent);
            status = "Order Created Successfully";
        } catch (Exception e) {
            order.setStatus("Order Failed");
            orderRepository.save(order);
            status = "Order Creation Failed";
        }
        return status;
    }

    /**
     * To listen to kafka event and do further operations.
     * @param event is read from kafka.
     */
    @KafkaListener(topics = "${kafka.topic.reverse-order}", groupId = "${kafka.topic.reverse-order-group}")
    public void reverseOrder(String event) {
        log.info("Reverse order event :: {}", event);
        try {
            OrderEvent orderEvent = objectMapper.readValue(event, OrderEvent.class);
            orderRepository.findById(orderEvent.customerOrder().getOrderId())
                    .ifPresent(order -> {
                        order.setStatus("Order Failed");
                        orderRepository.save(order);
                    });
        } catch (JsonProcessingException e) {
            log.error("Event parsing exception :: {}", e.getMessage());
        }
    }

}
