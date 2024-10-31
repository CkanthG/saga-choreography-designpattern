package com.sree.delivery.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.delivery.dto.CustomerOrder;
import com.sree.delivery.dto.DeliveryEvent;
import com.sree.delivery.entity.Delivery;
import com.sree.delivery.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

class DeliveryServiceTest {

    @InjectMocks
    DeliveryService deliveryService;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private ObjectMapper mapper;
    @Mock
    private KafkaTemplate<String, DeliveryEvent> kafkaTemplate;
    private DeliveryEvent deliveryEvent;
    private static final String VALID_EVENT_JSON = """
                {"customerOrder":{"item":"books","quantity":10,"amount":2000.0,"paymentMode":"CREDIT CARD",
                "orderId":102,"address":"Germany"},"type":"STOCK_UPDATED"}
                """;
    private static final String MISSING_ADDRESS_JSON = """
                {"customerOrder":{"item":"books","quantity":10,"amount":2000.0,"paymentMode":"CREDIT CARD",
                "orderId":102},"type":"STOCK_UPDATED"}
                """;
    private static final String INVALID_JSON = "invalid_json";
    private Delivery shipment;
    private CustomerOrder customerOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // given
        customerOrder = new CustomerOrder();
        customerOrder.setItem("books");
        customerOrder.setQuantity(10);
        customerOrder.setAmount(2000.0);
        customerOrder.setPaymentMode("CREDIT CARD");
        customerOrder.setOrderId(102);

        shipment = new Delivery();
        shipment.setOrderId(customerOrder.getOrderId());
    }

    @Test
    void testDeliverOrderSuccessfulDelivery() throws JsonProcessingException {
        //given
        customerOrder.setAddress("Germany");
        shipment.setAddress(customerOrder.getAddress());
        shipment.setStatus("Delivered Successfully");
        deliveryEvent = new DeliveryEvent(customerOrder, "STOCK_UPDATED");

        // when
        when(mapper.readValue(VALID_EVENT_JSON, DeliveryEvent.class)).thenReturn(deliveryEvent);
        when(deliveryRepository.save(shipment)).thenReturn(shipment);

        // then
        deliveryService.deliverOrder(VALID_EVENT_JSON);

        // verify
        verify(mapper, times(1)).readValue(VALID_EVENT_JSON, DeliveryEvent.class);
        verify(deliveryRepository, times(1)).save(shipment);
    }

    @Test
    void testDeliverOrderAddressMissing() throws JsonProcessingException {
        //given
        deliveryEvent = new DeliveryEvent(customerOrder, "STOCK_UPDATED");
        shipment.setStatus("Delivery Failed");

        // when
        when(mapper.readValue(MISSING_ADDRESS_JSON, DeliveryEvent.class)).thenReturn(deliveryEvent);
        when(deliveryRepository.save(shipment)).thenReturn(shipment);

        // then
        deliveryService.deliverOrder(MISSING_ADDRESS_JSON);

        // verify
        verify(mapper, times(1)).readValue(MISSING_ADDRESS_JSON, DeliveryEvent.class);
        verify(deliveryRepository, times(1)).save(shipment);
    }

    @Test
    void testDeliverOrderInvalidJson() throws JsonProcessingException {
        // when
        when(mapper.readValue(INVALID_JSON, DeliveryEvent.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});

        // then
        deliveryService.deliverOrder(INVALID_JSON);

        // verify
        verify(mapper, times(1)).readValue(INVALID_JSON, DeliveryEvent.class);
    }
}