package com.sree.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.payment.dto.CustomerOrder;
import com.sree.payment.dto.OrderEvent;
import com.sree.payment.dto.PaymentEvent;
import com.sree.payment.entity.Payment;
import com.sree.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private ObjectMapper mapper;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private KafkaTemplate<String, PaymentEvent> kafkaPaymentTemplate;
    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaOrderTemplate;
    private static final String VALID_EVENT_JSON = """
                {"customerOrder":{"item":"books","quantity":10,"amount":2000.0,"paymentMode":"CREDIT CARD",
                "orderId":102,"address":"Germany"},"type":"ORDER_CREATED"}
                """;
    private static final String VALID_PAYMENT_EVENT_JSON = """
                {"customerOrder":{"item":"books","quantity":10,"amount":2000.0,"paymentMode":"CREDIT CARD",
                "orderId":102,"address":"Germany"},"type":"PAYMENT_CREATED"}
                """;
    private static final String INVALID_JSON = "invalid_json";
    private CustomerOrder customerOrder;
    private Payment payment;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // given
        customerOrder = new CustomerOrder();
        customerOrder.setItem("books");
        customerOrder.setQuantity(10);
        customerOrder.setAmount(2000.0);
        customerOrder.setPaymentMode("CREDIT CARD");
        payment = new Payment();
    }

    @Test
    void testProcessPaymentSuccessful() throws JsonProcessingException {
        // given
        customerOrder.setOrderId(102);
        OrderEvent orderEvent = new OrderEvent(customerOrder, "ORDER_CREATED");
        Integer orderId = customerOrder.getOrderId();
        payment.setPaymentMode(customerOrder.getPaymentMode());
        payment.setAmount(customerOrder.getAmount());
        payment.setOrderId(orderId);
        payment.setStatus("Payment Success");

        // when
        when(mapper.readValue(VALID_EVENT_JSON, OrderEvent.class)).thenReturn(orderEvent);
        when(paymentRepository.save(payment)).thenReturn(payment);

        // then
        paymentService.processPayment(VALID_EVENT_JSON);

        // verify
        verify(mapper, times(1)).readValue(VALID_EVENT_JSON, OrderEvent.class);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void testProcessPaymentFailedWithValidJson() throws JsonProcessingException {
        // given
        payment.setStatus("Payment Failed");

        // when
        when(mapper.readValue(VALID_EVENT_JSON, OrderEvent.class)).thenReturn(null);
        when(paymentRepository.save(payment)).thenReturn(payment);

        // then
        paymentService.processPayment(VALID_EVENT_JSON);

        // verify
        verify(mapper, times(1)).readValue(VALID_EVENT_JSON, OrderEvent.class);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void testProcessPaymentFailedWithInValidJson() throws JsonProcessingException {
        // when
        when(mapper.readValue(INVALID_JSON, OrderEvent.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});

        // then
        paymentService.processPayment(INVALID_JSON);

        // verify
        verify(mapper, times(1)).readValue(INVALID_JSON, OrderEvent.class);
    }

    @Test
    void testReversePaymentsSuccessful() throws JsonProcessingException {
        // given
        PaymentEvent paymentEvent = new PaymentEvent(customerOrder, "PAYMENT_CREATED");

        // when
        when(mapper.readValue(VALID_PAYMENT_EVENT_JSON, PaymentEvent.class)).thenReturn(paymentEvent);
        when(paymentRepository.findByOrderId(customerOrder.getOrderId())).thenReturn(List.of(payment));

        // then
        paymentService.reversePayments(VALID_PAYMENT_EVENT_JSON);

        // verify
        verify(mapper, times(1)).readValue(VALID_PAYMENT_EVENT_JSON, PaymentEvent.class);
        verify(paymentRepository, times(1)).findByOrderId(customerOrder.getOrderId());
    }

    @Test
    void testReversePaymentsFailed() throws JsonProcessingException {
        // when
        when(mapper.readValue(INVALID_JSON, PaymentEvent.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});

        // then
        paymentService.reversePayments(INVALID_JSON);

        // verify
        verify(mapper, times(1)).readValue(INVALID_JSON, PaymentEvent.class);
    }
}
