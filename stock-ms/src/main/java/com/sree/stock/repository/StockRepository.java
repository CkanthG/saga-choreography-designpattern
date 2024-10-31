package com.sree.stock.repository;

import com.sree.stock.entity.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * To do CRUD operations on WareHouse table.
 */
@Repository
public interface StockRepository extends JpaRepository<WareHouse, Integer> {
    List<WareHouse> findByItem(String item);
}
