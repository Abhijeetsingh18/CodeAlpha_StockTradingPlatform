package StockTradingPlatform_Java.src;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single tradeable stock: its symbol, name, current price,
 * and a rolling history of price points (used for charts / day-change calc).
 */
public class Stock implements Serializable {
    private static final long serialVersionUID = 1L;

    /** A single (timestamp, price) point in a stock's history. */
    public static class PricePoint implements Serializable {
        private static final long serialVersionUID = 1L;
        public final LocalDateTime timestamp;
        public final double price;

        public PricePoint(LocalDateTime timestamp, double price) {
            this.timestamp = timestamp;
            this.price = price;
        }
    }

    private final String symbol;
    private final String name;
    private double price;
    private double volatility;      // used by Market to simulate price moves
    private double openPrice;       // price at "market open" / session start
    private final List<PricePoint> priceHistory = new ArrayList<>();

    public Stock(String symbol, String name, double price, double volatility) {
        this.symbol = symbol.toUpperCase();
        this.name = name;
        this.price = round2(price);
        this.volatility = volatility;
        this.openPrice = this.price;
        this.priceHistory.add(new PricePoint(LocalDateTime.now(), this.price));
    }

    public void updatePrice(double newPrice) {
        this.price = round2(Math.max(0.01, newPrice));
        this.priceHistory.add(new PricePoint(LocalDateTime.now(), this.price));
        // keep history from growing unbounded
        if (priceHistory.size() > 500) {
            priceHistory.subList(0, priceHistory.size() - 500).clear();
        }
    }

    public void resetOpenPrice() {
        this.openPrice = this.price;
    }

    public double getDayChange() {
        return round2(price - openPrice);
    }

    public double getDayChangePct() {
        if (openPrice == 0) return 0.0;
        return round2((getDayChange() / openPrice) * 100);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ---- getters -----------------------------------------------------
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getVolatility() { return volatility; }
    public double getOpenPrice() { return openPrice; }
    public List<PricePoint> getPriceHistory() { return priceHistory; }

    @Override
    public String toString() {
        String arrow = getDayChange() >= 0 ? "\u25B2" : "\u25BC";
        return String.format("%-6s%-20s$%9.2f  %s %+.2f%%",
                symbol, name, price, arrow, getDayChangePct());
    }
}
