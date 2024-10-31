package com.sree.stock.entity;

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
@Table(name = "WARE_HOUSE_TBL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WareHouse {
    @Id
    @GeneratedValue
    private Integer wareHouseId;
    private String item;
    private Integer quantity;
}
