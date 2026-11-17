package com.example.portfolio.exception;

public class InsufficientSharesException extends RuntimeException {

    public InsufficientSharesException(String symbol, int owned, int requested) {
        super("Cannot sell " + requested + " shares of " + symbol
                + ". You only own " + owned + ".");
    }
}