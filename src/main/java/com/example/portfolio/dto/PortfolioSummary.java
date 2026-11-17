package com.example.portfolio.dto;

import java.math.BigDecimal;

public record PortfolioSummary(
        BigDecimal totalCost,
        BigDecimal totalMarketValue,
        BigDecimal totalProfitLoss,
        int numberOfHoldings) {
}