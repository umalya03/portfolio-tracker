package com.example.portfolio.service;

import com.example.portfolio.dto.HoldingResponse;
import com.example.portfolio.dto.PortfolioSummary;
import com.example.portfolio.exception.InsufficientSharesException;
import com.example.portfolio.model.StockPrice;
import com.example.portfolio.model.Transaction;
import com.example.portfolio.model.TransactionType;
import com.example.portfolio.repository.StockPriceRepository;
import com.example.portfolio.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

    private final TransactionRepository transactionRepository;
    private final StockPriceRepository stockPriceRepository;

    // Spring sees this constructor and passes in the two repositories (dependency injection)
    public PortfolioService(TransactionRepository transactionRepository,
                            StockPriceRepository stockPriceRepository) {
        this.transactionRepository = transactionRepository;
        this.stockPriceRepository = stockPriceRepository;
    }

    @Transactional
    public Transaction recordTransaction(String symbol, TransactionType type,
                                         int quantity, BigDecimal price, LocalDate date) {
        String cleanSymbol = symbol.trim().toUpperCase();

        if (type == TransactionType.SELL) {
            int owned = sharesOwned(cleanSymbol);
            if (quantity > owned) {
                throw new InsufficientSharesException(cleanSymbol, owned, quantity);
            }
        }
        return transactionRepository.save(
                new Transaction(cleanSymbol, type, quantity, price, date));
    }

    @Transactional
    public StockPrice setPrice(String symbol, BigDecimal price) {
        String cleanSymbol = symbol.trim().toUpperCase();
        StockPrice stockPrice = stockPriceRepository.findById(cleanSymbol)
                .orElseGet(() -> new StockPrice(cleanSymbol, price));
        stockPrice.setPrice(price);
        return stockPriceRepository.save(stockPrice);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByDateAscIdAsc();
    }

    @Transactional(readOnly = true)
    public List<HoldingResponse> getHoldings() {
        Map<String, Position> positions = new LinkedHashMap<>();

        for (Transaction t : transactionRepository.findAllByOrderByDateAscIdAsc()) {
            positions.computeIfAbsent(t.getSymbol(), s -> new Position()).apply(t);
        }

        List<HoldingResponse> holdings = new ArrayList<>();
        for (Map.Entry<String, Position> entry : positions.entrySet()) {
            Position position = entry.getValue();
            if (position.shares == 0) {
                continue; // fully sold, nothing to show
            }
            holdings.add(toHolding(entry.getKey(), position));
        }
        return holdings;
    }

    @Transactional(readOnly = true)
    public PortfolioSummary getSummary() {
        List<HoldingResponse> holdings = getHoldings();

        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalProfitLoss = BigDecimal.ZERO;

        for (HoldingResponse h : holdings) {
            totalCost = totalCost.add(
                    h.averageCost().multiply(BigDecimal.valueOf(h.quantity())));
            totalMarketValue = totalMarketValue.add(h.marketValue());
            totalProfitLoss = totalProfitLoss.add(h.profitLoss());
        }

        return new PortfolioSummary(
                totalCost.setScale(2, RoundingMode.HALF_UP),
                totalMarketValue.setScale(2, RoundingMode.HALF_UP),
                totalProfitLoss.setScale(2, RoundingMode.HALF_UP),
                holdings.size());
    }

    private int sharesOwned(String symbol) {
        int owned = 0;
        for (Transaction t : transactionRepository.findAllByOrderByDateAscIdAsc()) {
            if (!t.getSymbol().equals(symbol)) {
                continue;
            }
            owned += (t.getType() == TransactionType.BUY) ? t.getQuantity() : -t.getQuantity();
        }
        return owned;
    }

    private HoldingResponse toHolding(String symbol, Position position) {
        BigDecimal averageCost = position.averageCost();

        // If no price has been set for this stock, fall back to the average cost
        BigDecimal currentPrice = stockPriceRepository.findById(symbol)
                .map(StockPrice::getPrice)
                .orElse(averageCost);

        BigDecimal shares = BigDecimal.valueOf(position.shares);
        BigDecimal marketValue = currentPrice.multiply(shares);
        BigDecimal profitLoss = currentPrice.subtract(averageCost).multiply(shares);

        return new HoldingResponse(
                symbol,
                position.shares,
                averageCost,
                currentPrice,
                marketValue.setScale(2, RoundingMode.HALF_UP),
                profitLoss.setScale(2, RoundingMode.HALF_UP));
    }

    // Tracks one stock's running position as we replay its transactions in order
    private static class Position {
        private int shares = 0;
        private BigDecimal totalCost = BigDecimal.ZERO;

        void apply(Transaction t) {
            BigDecimal quantity = BigDecimal.valueOf(t.getQuantity());

            if (t.getType() == TransactionType.BUY) {
                totalCost = totalCost.add(t.getPrice().multiply(quantity));
                shares += t.getQuantity();
            } else {
                // a sell removes shares at the current average cost, so the average doesn't change
                totalCost = totalCost.subtract(averageCost().multiply(quantity));
                shares -= t.getQuantity();
                if (shares == 0) {
                    totalCost = BigDecimal.ZERO; // clear any rounding leftovers
                }
            }
        }

        BigDecimal averageCost() {
            if (shares == 0) {
                return BigDecimal.ZERO;
            }
            return totalCost.divide(BigDecimal.valueOf(shares), 4, RoundingMode.HALF_UP);
        }
    }
}