package com.sree.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.payment.dto.CustomerOrder;
import com.sree.payment.dto.OrderEvent;
import com.sree.payment.dto.PaymentEvent;
import com.sree.payment.entity.Payment;
import com.sree.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * To handle all payment related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ObjectMapper mapper;
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaPaymentTemplate;
    private final KafkaTemplate<String, OrderEvent> kafkaOrderTemplate;
    @Value("${kafka.topic.new-payments:new-payments}")
    private String paymentTopic;
    @Value("${kafka.topic.reverse-order:reverse-order}")
    private String reverseOrderTopic;

    /**
     * To listen kafka topic and do further action.
     * @param event is read from kafka topic.
     */
    @KafkaListener(topics = "${kafka.topic.new-order}", groupId = "${spring.kafka.consumer.group-id}")
    public void processPayment(String event) {
        log.info("Process payment event :: {}", event);
        Payment payment = new Payment();
        Integer orderId = null;
        CustomerOrder order = null;
        try {
            OrderEvent orderEvent = mapper.readValue(event, OrderEvent.class);
            order = orderEvent.customerOrder();
            orderId = order.getOrderId();
            payment.setPaymentMode(order.getPaymentMode());
            payment.setAmount(order.getAmount());
            payment.setOrderId(orderId);
            payment.setStatus("Payment Success");
            paymentRepository.save(payment);
            PaymentEvent paymentEvent = new PaymentEvent(order, "PAYMENT_CREATED");
            kafkaPaymentTemplate.send(paymentTopic, paymentEvent);
        } catch (JsonProcessingException e) {
            log.error("Process Event Parsing Exception :: {}", e.getMessage());
        } catch (Exception e) {
            payment.setOrderId(orderId);
            payment.setStatus("Payment Failed");
            paymentRepository.save(payment);
            OrderEvent orderEvent = new OrderEvent(order, "ORDER_REVERSED");
            kafkaOrderTemplate.send(reverseOrderTopic, orderEvent);
        }
    }

    /**
     * To listen kafka topic and do further action.
     * @param event is read from kafka topic.
     */
    @KafkaListener(topics = "${kafka.topic.reverse-payments}", groupId = "${kafka.topic.reverse-payment-group}")
    public void reversePayments(String event) {
        log.info("Reverse Payment for order :: {}", event);
        try {
            PaymentEvent paymentEvent = mapper.readValue(event, PaymentEvent.class);
            CustomerOrder customerOrder = paymentEvent.customerOrder();
            paymentRepository.findByOrderId(customerOrder.getOrderId())
                    .forEach(
                            payment -> {
                                payment.setStatus("Payment Failed");
                                paymentRepository.save(payment);
                            }
                    );
            OrderEvent orderEvent = new OrderEvent(paymentEvent.customerOrder(), "ORDER_REVERSED");
            kafkaOrderTemplate.send(reverseOrderTopic, orderEvent);
        } catch (JsonProcessingException e) {
            log.error("Payment Process Exception :: {}", e.getMessage());
        }
    }
}
