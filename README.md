# Portfolio Tracker API

A Spring Boot REST API for tracking a stock portfolio. You record buy and sell
transactions, and the API works out your current holdings, average cost, and
profit/loss.

## Features
- Record BUY and SELL transactions
- Rejects selling more shares than you own
- Calculates holdings with weighted average cost
- Profit/loss per stock and for the whole portfolio
- Request validation with clear error messages

## Tech stack
- Java 21, Spring Boot 4.1
- Spring Web, Spring Data JPA (Hibernate), Bean Validation
- H2 in-memory database
- Maven
- JUnit 5, Mockito, AssertJ

## Architecture
Layered design, where each layer only talks to the one below it:

```
Controller  ->  Service  ->  Repository  ->  H2 database
```

- **controller**: REST endpoints, request/response handling
- **service**: business rules and calculations
- **repository**: Spring Data JPA interfaces
- **model**: JPA entities (`Transaction`, `StockPrice`)
- **dto**: request and response objects
- **exception**: custom exceptions and a global error handler

## API endpoints

| Method | URL                      | Description                       |
|--------|--------------------------|-----------------------------------|
| POST   | /api/transactions        | Record a buy or sell              |
| GET    | /api/transactions        | List all transactions             |
| PUT    | /api/prices/{symbol}     | Set the current price of a stock  |
| GET    | /api/holdings            | Holdings with profit/loss         |
| GET    | /api/portfolio/summary   | Portfolio totals                  |

Ready-made example requests are in [`requests.http`](requests.http)
(works with the VS Code REST Client extension).

### Example
Buy 10 shares at 150 and 10 at 170, sell 5, then set the price to 180.

`GET /api/holdings` returns:
```json
[
  {
    "symbol": "AAPL",
    "quantity": 15,
    "averageCost": 160.0000,
    "currentPrice": 180.0000,
    "marketValue": 2700.00,
    "profitLoss": 300.00
  }
]
```

## Run it
Requires JDK 21 or newer.

```
./mvnw spring-boot:run
```
On Windows PowerShell use `.\mvnw spring-boot:run`. The API runs at
http://localhost:8080. Data is stored in memory, so it resets on restart.

## Run the tests
```
./mvnw test
```

## Design decisions
- **BigDecimal for money**, because double can lose precision with decimals.
- **Holdings are calculated from transactions** instead of stored, so the
  numbers can't get out of sync.
- **Weighted average cost**: a buy updates the average cost, a sell does not.
- **Global exception handler** returns consistent 400 responses instead of stack traces.

## Limitations and future work
- In-memory database (a PostgreSQL setup would be the next step)
- Stock prices are entered manually, not from a market data feed
- Single portfolio, no user accounts or authentication