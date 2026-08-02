package StockTradingPlatform_Java.src;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds a user's cash balance, stock holdings, average cost basis,
 * full transaction history, and timestamped value snapshots used
 * to track portfolio performance over time.
 */
public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    /** A single position's unrealized gain/loss vs. its average cost basis. */
    public static class PositionReport implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String symbol;
        public final int quantity;
        public final double avgCost;
        public final double currentPrice;
        public final double marketValue;
        public final double gainLoss;
        public final double gainLossPct;

        public PositionReport(String symbol, int quantity, double avgCost, double currentPrice,
                               double marketValue, double gainLoss, double gainLossPct) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.avgCost = avgCost;
            this.currentPrice = currentPrice;
            this.marketValue = marketValue;
            this.gainLoss = gainLoss;
            this.gainLossPct = gainLossPct;
        }
    }

    /** A timestamped snapshot of total portfolio value, for performance-over-time tracking. */
    public static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        public final LocalDateTime timestamp;
        public final double cash;
        public final double holdingsValue;
        public final double totalValue;

        public Snapshot(LocalDateTime timestamp, double cash, double holdingsValue, double totalValue) {
            this.timestamp = timestamp;
            this.cash = cash;
            this.holdingsValue = holdingsValue;
            this.totalValue = totalValue;
        }
    }

    private double cashBalance;
    private final Map<String, Integer> holdings = new LinkedHashMap<>();
    private final Map<String, Double> avgCost = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Snapshot> history = new ArrayList<>();

    public Portfolio(double startingCash) {
        this.cashBalance = startingCash;
    }

    // ---- trading operations ------------------------------------------------
    public Transaction buy(Stock stock, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        double cost = stock.getPrice() * quantity;
        if (cost > cashBalance) {
            throw new IllegalArgumentException(
                    String.format("Insufficient funds: need $%,.2f, have $%,.2f", cost, cashBalance));
        }

        int prevQty = holdings.getOrDefault(stock.getSymbol(), 0);
        double prevCost = avgCost.getOrDefault(stock.getSymbol(), 0.0);
        int newQty = prevQty + quantity;
        double newAvgCost = ((prevCost * prevQty) + cost) / newQty;
        avgCost.put(stock.getSymbol(), newAvgCost);

        cashBalance -= cost;
        holdings.put(stock.getSymbol(), newQty);

        Transaction txn = new Transaction(stock.getSymbol(), Transaction.Action.BUY, quantity, stock.getPrice());
        transactions.add(txn);
        return txn;
    }

    public Transaction sell(Stock stock, int quantity) {
        int owned = holdings.getOrDefault(stock.getSymbol(), 0);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (quantity > owned) {
            throw new IllegalArgumentException(
                    String.format("You only own %d shares of %s.", owned, stock.getSymbol()));
        }

        double proceeds = stock.getPrice() * quantity;
        cashBalance += proceeds;
        int remaining = owned - quantity;
        if (remaining == 0) {
            holdings.remove(stock.getSymbol());
            avgCost.remove(stock.getSymbol());
        } else {
            holdings.put(stock.getSymbol(), remaining);
        }

        Transaction txn = new Transaction(stock.getSymbol(), Transaction.Action.SELL, quantity, stock.getPrice());
        transactions.add(txn);
        return txn;
    }

    // ---- valuation -----------------------------------------------------
    public double holdingsValue(Market market) {
        double total = 0.0;
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            Stock stock = market.getStock(entry.getKey());
            if (stock != null) {
                total += stock.getPrice() * entry.getValue();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public double totalValue(Market market) {
        return Math.round((cashBalance + holdingsValue(market)) * 100.0) / 100.0;
    }

    public List<PositionReport> unrealizedGainLoss(Market market) {
        List<PositionReport> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            String symbol = entry.getKey();
            int qty = entry.getValue();
            Stock stock = market.getStock(symbol);
            if (stock == null) continue;

            double costBasis = avgCost.getOrDefault(symbol, 0.0);
            double marketValue = stock.getPrice() * qty;
            double invested = costBasis * qty;
            double gain = marketValue - invested;
            double gainPct = invested != 0 ? (gain / invested) * 100 : 0.0;

            result.add(new PositionReport(symbol, qty,
                    Math.round(costBasis * 100.0) / 100.0,
                    stock.getPrice(),
                    Math.round(marketValue * 100.0) / 100.0,
                    Math.round(gain * 100.0) / 100.0,
                    Math.round(gainPct * 100.0) / 100.0));
        }
        return result;
    }

    /** Save a timestamped snapshot of total portfolio value (for performance-over-time tracking). */
    public void recordSnapshot(Market market) {
        history.add(new Snapshot(LocalDateTime.now(), round2(cashBalance), holdingsValue(market), totalValue(market)));
        if (history.size() > 1000) {
            history.subList(0, history.size() - 1000).clear();
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ---- getters -----------------------------------------------------
    public double getCashBalance() { return cashBalance; }
    public Map<String, Integer> getHoldings() { return holdings; }
    public Map<String, Double> getAvgCost() { return avgCost; }
    public List<Transaction> getTransactions() { return transactions; }
    public List<Snapshot> getHistory() { return history; }
}
