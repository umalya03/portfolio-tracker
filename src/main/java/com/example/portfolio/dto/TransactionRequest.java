package com.example.portfolio.dto;

import com.example.portfolio.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        String symbol,
        TransactionType type,
        Integer quantity,
        BigDecimal price,
        LocalDate date) {
}