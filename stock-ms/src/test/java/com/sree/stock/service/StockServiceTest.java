package com.sree.stock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.stock.dto.CustomerOrder;
import com.sree.stock.dto.DeliveryEvent;
import com.sree.stock.dto.PaymentEvent;
import com.sree.stock.dto.Stock;
import com.sree.stock.entity.WareHouse;
import com.sree.stock.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockServiceTest {

    @InjectMocks
    private StockService stockService;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ObjectMapper mapper;
    @Mock
    private KafkaTemplate<String, DeliveryEvent> kafkaDeliveryEventTemplate;
    @Mock
    private KafkaTemplate<String, PaymentEvent> kafkaPaymentEventTemplate;
    private CustomerOrder customerOrder;
    WareHouse wareHouse;
    private static final String VALID_EVENT_JSON = """
                {"customerOrder":{"item":"books","quantity":10,"amount":2000.0,"paymentMode":"CREDIT CARD",
                "orderId":102,"address":"Germany"},"type":"PAYMENT_CREATED"}
                """;
    private static final String INVALID_JSON = "invalid_json";
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // given
        customerOrder = new CustomerOrder();
        customerOrder.setItem("books");
        customerOrder.setQuantity(10);
        customerOrder.setAmount(2000.0);
        customerOrder.setPaymentMode("CREDIT CARD");
    }

    @Test
    void testUpdateStockSuccessful() throws JsonProcessingException {
        // given
        wareHouse = new WareHouse();
        wareHouse.setItem("books");
        wareHouse.setQuantity(100);
        PaymentEvent paymentEvent = new PaymentEvent(customerOrder, "PAYMENT_CREATED");

        // when
        when(mapper.readValue(VALID_EVENT_JSON, PaymentEvent.class)).thenReturn(paymentEvent);
        when(stockRepository.findByItem(customerOrder.getItem())).thenReturn(List.of(wareHouse));
        when(stockRepository.save(wareHouse)).thenReturn(wareHouse);

        // then
        stockService.updateStock(VALID_EVENT_JSON);

        // verify
        verify(mapper, times(1)).readValue(VALID_EVENT_JSON, PaymentEvent.class);
        verify(stockRepository, times(1)).findByItem(customerOrder.getItem());
        verify(stockRepository, times(1)).save(wareHouse);
    }

    @Test
    void testUpdateStockEmptyCheck() throws JsonProcessingException {
        // given
        PaymentEvent paymentEvent = new PaymentEvent(customerOrder, "PAYMENT_CREATED");

        // when
        when(mapper.readValue(VALID_EVENT_JSON, PaymentEvent.class)).thenReturn(paymentEvent);
        when(stockRepository.findByItem(customerOrder.getItem())).thenReturn(List.of());

        // then
        stockService.updateStock(VALID_EVENT_JSON);

        // verify
        verify(mapper, times(1)).readValue(VALID_EVENT_JSON, PaymentEvent.class);
        verify(stockRepository, times(1)).findByItem(customerOrder.getItem());
    }

    @Test
    void testUpdateStockFailedWithInValidJson() throws JsonProcessingException {
        // when
        when(mapper.readValue(INVALID_JSON, PaymentEvent.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});

        // then
        stockService.updateStock(INVALID_JSON);

        // verify
        verify(mapper, times(1)).readValue(INVALID_JSON, PaymentEvent.class);
    }

    @Test
    void testReverseStockSuccessful() throws JsonProcessingException {
        // given
        wareHouse = new WareHouse();
        wareHouse.setItem("books");
        wareHouse.setQuantity(100);
        DeliveryEvent deliveryEvent = new DeliveryEvent(customerOrder, "PAYMENT_CREATED");

        // when
        when(mapper.readValue(VALID_EVENT_JSON, DeliveryEvent.class)).thenReturn(deliveryEvent);
        when(stockRepository.findByItem(customerOrder.getItem())).thenReturn(List.of(wareHouse));
        when(stockRepository.save(wareHouse)).thenReturn(wareHouse);

        // then
        stockService.reverseStock(VALID_EVENT_JSON);

        // verify
        verify(mapper, times(1)).readValue(VALID_EVENT_JSON, DeliveryEvent.class);
        verify(stockRepository, times(1)).findByItem(customerOrder.getItem());
        verify(stockRepository, times(1)).save(wareHouse);
    }

    @Test
    void testReverseStockFailed() throws JsonProcessingException {
        // when
        when(mapper.readValue(INVALID_JSON, DeliveryEvent.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});

        // then
        stockService.reverseStock(INVALID_JSON);

        // verify
        verify(mapper, times(1)).readValue(INVALID_JSON, DeliveryEvent.class);
    }

    @Test
    void testAddStockSuccessful() {
        // given
        Stock stock = new Stock("books", 10);
        wareHouse = new WareHouse();
        wareHouse.setItem(stock.item());
        wareHouse.setQuantity(stock.quantity());

        // when
        when(stockRepository.findByItem(customerOrder.getItem())).thenReturn(List.of());
        when(stockRepository.save(wareHouse)).thenReturn(wareHouse);

        // then
        var actual = stockService.addStock(stock);
        assertEquals("Successfully added stock", actual);

        // verify
        verify(stockRepository, times(1)).findByItem(customerOrder.getItem());
        verify(stockRepository, times(1)).save(wareHouse);
    }

    @Test
    void testUpdateExistingStockSuccessful() {
        // given
        Stock stock = new Stock("books", 10);
        wareHouse = new WareHouse();
        wareHouse.setItem(stock.item());
        wareHouse.setQuantity(stock.quantity());

        // when
        when(stockRepository.findByItem(customerOrder.getItem())).thenReturn(List.of(wareHouse));
        when(stockRepository.save(wareHouse)).thenReturn(wareHouse);

        // then
        var actual = stockService.addStock(stock);
        assertEquals("Successfully added stock", actual);

        // verify
        verify(stockRepository, times(1)).findByItem(customerOrder.getItem());
        verify(stockRepository, times(1)).save(wareHouse);
    }
}