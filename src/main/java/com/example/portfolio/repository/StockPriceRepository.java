package com.example.portfolio.repository;

import com.example.portfolio.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceRepository extends JpaRepository<StockPrice, String> {
}