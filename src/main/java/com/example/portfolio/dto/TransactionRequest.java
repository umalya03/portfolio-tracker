package com.example.portfolio.dto;

import com.example.portfolio.model.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(

        @NotBlank(message = "symbol is required")
        @Pattern(regexp = "^[A-Za-z.]{1,10}$",
                 message = "symbol must be 1-10 letters, for example AAPL")
        String symbol,

        @NotNull(message = "type is required (BUY or SELL)")
        TransactionType type,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be greater than 0")
        Integer quantity,

        @NotNull(message = "price is required")
        @Positive(message = "price must be greater than 0")
        @Digits(integer = 15, fraction = 4, message = "price can have at most 4 decimal places")
        BigDecimal price,

        @PastOrPresent(message = "date cannot be in the future")
        LocalDate date) {
}