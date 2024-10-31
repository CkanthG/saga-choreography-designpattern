package com.sree.delivery.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * To persist data in db.
 */
@Entity
@Table(name = "DELIVERY_TBL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {
    @Id
    @GeneratedValue
    private Integer deliveryId;
    private String address;
    private String status;
    private Integer orderId;
}
