package com.example.portfolio.service;

import com.example.portfolio.dto.HoldingResponse;
import com.example.portfolio.exception.InsufficientSharesException;
import com.example.portfolio.model.StockPrice;
import com.example.portfolio.model.Transaction;
import com.example.portfolio.model.TransactionType;
import com.example.portfolio.repository.StockPriceRepository;
import com.example.portfolio.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StockPriceRepository stockPriceRepository;

    @InjectMocks
    private PortfolioService service;

    private Transaction buy(String symbol, int quantity, String price, String date) {
        return new Transaction(symbol, TransactionType.BUY, quantity,
                new BigDecimal(price), LocalDate.parse(date));
    }

    private Transaction sell(String symbol, int quantity, String price, String date) {
        return new Transaction(symbol, TransactionType.SELL, quantity,
                new BigDecimal(price), LocalDate.parse(date));
    }

    @Test
    void calculatesAverageCostAndProfitLoss() {
        when(transactionRepository.findAllByOrderByDateAscIdAsc()).thenReturn(List.of(
                buy("AAPL", 10, "150", "2026-11-01"),
                buy("AAPL", 10, "170", "2026-11-05"),
                sell("AAPL", 5, "175", "2026-11-10")));
        when(stockPriceRepository.findById("AAPL"))
                .thenReturn(Optional.of(new StockPrice("AAPL", new BigDecimal("180"))));

        List<HoldingResponse> holdings = service.getHoldings();

        assertThat(holdings).hasSize(1);
        HoldingResponse h = holdings.get(0);
        assertThat(h.quantity()).isEqualTo(15);
        assertThat(h.averageCost()).isEqualByComparingTo("160");
        assertThat(h.marketValue()).isEqualByComparingTo("2700");
        assertThat(h.profitLoss()).isEqualByComparingTo("300");
    }

    @Test
    void rejectsSellingMoreThanOwned() {
        when(transactionRepository.findAllByOrderByDateAscIdAsc())
                .thenReturn(List.of(buy("AAPL", 10, "150", "2026-11-01")));

        assertThatThrownBy(() -> service.recordTransaction(
                "AAPL", TransactionType.SELL, 11, new BigDecimal("175"), LocalDate.of(2026, 11, 10)))
                .isInstanceOf(InsufficientSharesException.class)
                .hasMessageContaining("only own 10");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void allowsSellingExactlyWhatYouOwn() {
        when(transactionRepository.findAllByOrderByDateAscIdAsc())
                .thenReturn(List.of(buy("AAPL", 10, "150", "2026-11-01")));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction saved = service.recordTransaction(
                "AAPL", TransactionType.SELL, 10, new BigDecimal("175"), LocalDate.of(2026, 11, 10));

        assertThat(saved.getType()).isEqualTo(TransactionType.SELL);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void hidesFullySoldPositions() {
        when(transactionRepository.findAllByOrderByDateAscIdAsc()).thenReturn(List.of(
                buy("AAPL", 10, "150", "2026-11-01"),
                sell("AAPL", 10, "160", "2026-11-05")));

        assertThat(service.getHoldings()).isEmpty();
    }

    @Test
    void usesAverageCostWhenNoPriceIsSet() {
        when(transactionRepository.findAllByOrderByDateAscIdAsc())
                .thenReturn(List.of(buy("AAPL", 10, "150", "2026-11-01")));
        when(stockPriceRepository.findById("AAPL")).thenReturn(Optional.empty());

        HoldingResponse h = service.getHoldings().get(0);

        assertThat(h.currentPrice()).isEqualByComparingTo("150");
        assertThat(h.profitLoss()).isEqualByComparingTo("0");
    }

    @Test
    void normalisesSymbolToUpperCase() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction saved = service.recordTransaction(
                "  aapl ", TransactionType.BUY, 5, new BigDecimal("150"), LocalDate.of(2026, 11, 1));

        assertThat(saved.getSymbol()).isEqualTo("AAPL");
    }
}