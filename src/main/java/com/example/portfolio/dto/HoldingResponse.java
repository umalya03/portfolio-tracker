package com.example.portfolio.dto;

import java.math.BigDecimal;

public record HoldingResponse(
        String symbol,
        int quantity,
        BigDecimal averageCost,
        BigDecimal currentPrice,
        BigDecimal marketValue,
        BigDecimal profitLoss) {
}