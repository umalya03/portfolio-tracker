package com.example.portfolio;

import com.example.portfolio.dto.PortfolioSummary;
import com.example.portfolio.model.TransactionType;
import com.example.portfolio.service.PortfolioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // each test rolls back its database changes when it finishes
class PortfolioIntegrationTest {

    @Autowired
    private PortfolioService service;

    @Test
    void workedExampleGivesProfitOf300() {
        service.recordTransaction("AAPL", TransactionType.BUY, 10,
                new BigDecimal("150"), LocalDate.of(2026, 11, 1));
        service.recordTransaction("AAPL", TransactionType.BUY, 10,
                new BigDecimal("170"), LocalDate.of(2026, 11, 5));
        service.recordTransaction("AAPL", TransactionType.SELL, 5,
                new BigDecimal("175"), LocalDate.of(2026, 11, 10));
        service.setPrice("AAPL", new BigDecimal("180"));

        PortfolioSummary summary = service.getSummary();

        assertThat(summary.numberOfHoldings()).isEqualTo(1);
        assertThat(summary.totalCost()).isEqualByComparingTo("2400");
        assertThat(summary.totalMarketValue()).isEqualByComparingTo("2700");
        assertThat(summary.totalProfitLoss()).isEqualByComparingTo("300");
    }
}