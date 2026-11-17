package com.example.portfolio.controller;

import com.example.portfolio.dto.HoldingResponse;
import com.example.portfolio.dto.PortfolioSummary;
import com.example.portfolio.dto.PriceRequest;
import com.example.portfolio.dto.TransactionRequest;
import com.example.portfolio.model.StockPrice;
import com.example.portfolio.model.Transaction;
import com.example.portfolio.service.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction addTransaction(@Valid @RequestBody TransactionRequest request) {
        LocalDate date = request.date() != null ? request.date() : LocalDate.now();
        return portfolioService.recordTransaction(
                request.symbol(), request.type(), request.quantity(), request.price(), date);
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactions() {
        return portfolioService.getAllTransactions();
    }

    @PutMapping("/prices/{symbol}")
    public StockPrice setPrice(@PathVariable String symbol, @Valid @RequestBody PriceRequest request) {
        return portfolioService.setPrice(symbol, request.price());
    }

    @GetMapping("/holdings")
    public List<HoldingResponse> getHoldings() {
        return portfolioService.getHoldings();
    }

    @GetMapping("/portfolio/summary")
    public PortfolioSummary getSummary() {
        return portfolioService.getSummary();
    }
}