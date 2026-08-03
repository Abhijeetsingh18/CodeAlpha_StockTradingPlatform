# 📈 StockTradingPlatform_Java.src.Stock Trading Platform (Java / OOP)

A complete, runnable simulation of a basic stock trading environment — built with
object-oriented Java and file-based persistence. Standard library only (no external
dependencies, no build tool required).

## Features

- **StockTradingPlatform_Java.src.Market data display** for 10 seeded stocks, with a random-walk price simulator
  you can advance manually ("simulate market ticks").
- **Buy / Sell operations** with cash-balance checks, average cost-basis tracking,
  and unrealized gain/loss per position.
- **StockTradingPlatform_Java.src.Portfolio performance over time** — every trade or market tick records a
  timestamped snapshot (cash, holdings value, total value) so you can view a
  performance history table with overall change %.
- **Multi-user support** — register/login as different users, each with their own
  portfolio, cash balance and transaction history.
- **Persistence** — market and user/portfolio data is saved to `data/market.dat`
  and `data/users.dat` via Java object serialization, and reloaded automatically
  the next time you run the program.
- **Clean OOP architecture** — see below.

## Project Structure

```
StockTradingPlatform/
├── src/
│   ├── Main.java              # CLI entry point (menu-driven interface)
│   ├── StockTradingPlatform_Java.src.TradingPlatform.java   # Top-level controller class
│   ├── StockTradingPlatform_Java.src.Market.java            # Owns StockTradingPlatform_Java.src.Stock objects + price simulation
│   ├── StockTradingPlatform_Java.src.Stock.java             # StockTradingPlatform_Java.src.Stock: symbol, price, price history
│   ├── StockTradingPlatform_Java.src.Transaction.java       # Immutable buy/sell trade record
│   ├── StockTradingPlatform_Java.src.Portfolio.java         # Cash, holdings, cost basis, history
│   ├── StockTradingPlatform_Java.src.User.java              # Username + owns a StockTradingPlatform_Java.src.Portfolio
│   └── StockTradingPlatform_Java.src.FileStorage.java       # Save/load persistence layer
├── data/                      # Auto-created — market.dat & users.dat live here
└── README.md
```

## Class Design (OOP)

| Class             | Responsibility                                                          |
|-------------------|---------------------------------------------------------------------------|
| `StockTradingPlatform_Java.src.Stock`           | Symbol, name, current price, price history, day-change calculations     |
| `StockTradingPlatform_Java.src.Market`          | Owns all `StockTradingPlatform_Java.src.Stock`s; simulates price movement (`tick()`); market display  |
| `StockTradingPlatform_Java.src.Transaction`     | Immutable record of one buy/sell trade                                  |
| `StockTradingPlatform_Java.src.Portfolio`       | Cash balance, holdings, avg. cost basis, transactions, value history    |
| `StockTradingPlatform_Java.src.User`            | Username + owns a `StockTradingPlatform_Java.src.Portfolio`                                            |
| `StockTradingPlatform_Java.src.TradingPlatform` | Orchestrates users, market and persistence; the main API surface         |
| `StockTradingPlatform_Java.src.FileStorage`     | Reads/writes `StockTradingPlatform_Java.src.Market` and `Map<String, StockTradingPlatform_Java.src.User>` objects to disk            |
| `Main`            | Menu-driven command-line interface                                       |

## Building & Running

Requires a JDK (Java 11+). From the `src/` directory:

```bash
# Compile
javac -d ../bin *.java

# Run
cd ..
java -cp bin Main
```

Or, in one line from the project root:

```bash
javac -d bin src/*.java && java -cp bin Main
```

> **Note:** If your environment has a JRE but no `javac` on the PATH (rare, but
> can happen in some sandboxes), you can still compile via the compiler module
> directly: `java -m jdk.compiler/com.sun.tools.javac.Main -d bin src/*.java`

You'll see a menu:

```
1) View StockTradingPlatform_Java.src.Market Data
2) Register New StockTradingPlatform_Java.src.User
3) Login
4) Buy StockTradingPlatform_Java.src.Stock
5) Sell StockTradingPlatform_Java.src.Stock
6) View StockTradingPlatform_Java.src.Portfolio
7) View Performance Over Time
8) View StockTradingPlatform_Java.src.Transaction History
9) Simulate StockTradingPlatform_Java.src.Market Movement
0) Exit
```

### Typical session

1. **Register** a user (choose a username and starting cash, default $10,000).
2. **Login** as that user.
3. **View StockTradingPlatform_Java.src.Market Data** to see current prices for AAPL, TSLA, NVDA, etc.
4. **Buy** some shares — funds are deducted from your cash balance.
5. **Simulate StockTradingPlatform_Java.src.Market Movement** to advance prices (like time passing).
6. **View StockTradingPlatform_Java.src.Portfolio** to see your updated holdings, value, and gain/loss.
7. **View Performance Over Time** to see your portfolio's value trend.
8. **Sell** shares whenever you like — proceeds are added back to cash.

All actions are saved automatically to `data/market.dat` and `data/users.dat`,
so you can close the program and resume later with your data intact.

## Using It Programmatically

You can also drive the platform directly from Java code instead of the CLI:

```java
import StockTradingPlatform_Java.src.TradingPlatform;

TradingPlatform app = new TradingPlatform();
app.

registerUser("alice",10_000);
app.

login("alice");

app.

buy("AAPL",10);
app.

simulateMarketTicks(5);      // advance the market 5 steps
app.

sell("AAPL",4);

System.out.

println(app.getPortfolio().

totalValue(app.getMarket()));
        System.out.

println(app.performanceHistory());
        System.out.

println(app.transactionHistory());

        app.

save();   
```

## Extending It

- Swap `StockTradingPlatform_Java.src.FileStorage`'s Java serialization for a real database (e.g. SQLite via
  JDBC) — the getters on every model make this migration straightforward.
- Add password authentication to `StockTradingPlatform_Java.src.User`.
- Add limit orders / order types to `StockTradingPlatform_Java.src.Transaction`.
- Feed `StockTradingPlatform_Java.src.Market` with a real price API instead of the random-walk simulator.
- Build a Swing/JavaFX GUI on top of `StockTradingPlatform_Java.src.TradingPlatform` instead of the CLI.
