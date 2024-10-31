package com.sree.delivery.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.delivery.dto.CustomerOrder;
import com.sree.delivery.dto.DeliveryEvent;
import com.sree.delivery.entity.Delivery;
import com.sree.delivery.exception.AddressNotFoundException;
import com.sree.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * To handle all delivery related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ObjectMapper mapper;
    private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;
    @Value("${kafka.topic.reverse-stock}")
    private String reverseStockTopic;

    /**
     * To listen to kafka topic and do necessary action.
     * @param event is going to listen and do further operations.
     */
    @KafkaListener(topics = "${kafka.topic.new-stock}", groupId = "${kafka.topic.stock-group}")
    public void deliverOrder(String event) {
        log.info("Deliver order :: {}", event);
        CustomerOrder customerOrder = null;
        Delivery shipment = new Delivery();
        try {
            DeliveryEvent deliveryEvent = mapper.readValue(event, DeliveryEvent.class);
            customerOrder = deliveryEvent.customerOrder();
            if (customerOrder.getAddress() == null) {
                log.warn("There is no address to deliver the order");
                throw new AddressNotFoundException("No address available");
            }
            shipment.setAddress(customerOrder.getAddress());
            shipment.setOrderId(customerOrder.getOrderId());
            shipment.setStatus("Delivered Successfully");
            deliveryRepository.save(shipment);
        } catch (JsonProcessingException e) {
            log.error("Process delivery parsing error :: {}", e.getMessage());
        } catch (AddressNotFoundException e) {
            shipment.setOrderId(customerOrder.getOrderId());
            shipment.setStatus("Delivery Failed");
            deliveryRepository.save(shipment);
            log.error("Order :: {} failed to deliver.", customerOrder);
            DeliveryEvent deliveryEvent = new DeliveryEvent(customerOrder, "STOCK_REVERSED");
            kafkaTemplate.send(reverseStockTopic, deliveryEvent);
        }
    }
}
