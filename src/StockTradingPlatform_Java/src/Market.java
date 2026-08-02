package StockTradingPlatform_Java.src;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Owns all Stock objects and simulates price movement over time
 * (a simple random-walk scaled by each stock's volatility).
 */
public class Market implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Stock> stocks = new LinkedHashMap<>();
    private final Random random = new Random();

    /** symbol, name, starting price, volatility */
    private static final Object[][] DEFAULT_LISTINGS = {
            {"AAPL", "Apple Inc.", 195.50, 0.015},
            {"GOOGL", "Alphabet Inc.", 164.30, 0.018},
            {"MSFT", "Microsoft Corp.", 420.10, 0.014},
            {"AMZN", "Amazon.com Inc.", 178.25, 0.020},
            {"TSLA", "Tesla Inc.", 238.90, 0.045},
            {"NVDA", "NVIDIA Corp.", 118.75, 0.035},
            {"META", "Meta Platforms Inc.", 505.60, 0.025},
            {"NFLX", "Netflix Inc.", 680.40, 0.022},
            {"JPM", "JPMorgan Chase & Co.", 205.15, 0.012},
            {"DIS", "Walt Disney Co.", 98.60, 0.020},
    };

    public Market(boolean seedDefaults) {
        if (seedDefaults) {
            for (Object[] row : DEFAULT_LISTINGS) {
                addStock(new Stock((String) row[0], (String) row[1], (Double) row[2], (Double) row[3]));
            }
        }
    }

    // ---- stock management --------------------------------------------
    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol(), stock);
    }

    public Stock getStock(String symbol) {
        if (symbol == null) return null;
        return stocks.get(symbol.toUpperCase());
    }

    public List<String> listSymbols() {
        List<String> symbols = new ArrayList<>(stocks.keySet());
        Collections.sort(symbols);
        return symbols;
    }

    public Map<String, Stock> getStocks() {
        return stocks;
    }

    // ---- simulation -----------------------------------------------------
    /** Advance market prices by one simulated step for every stock. */
    public void tick() {
        for (Stock stock : stocks.values()) {
            double pctMove = random.nextGaussian() * stock.getVolatility();
            double newPrice = stock.getPrice() * (1 + pctMove);
            stock.updatePrice(newPrice);
        }
    }

    /** Mark current prices as the new "open" (e.g. start of a trading day). */
    public void resetSessionOpen() {
        for (Stock stock : stocks.values()) {
            stock.resetOpenPrice();
        }
    }

    // ---- display -----------------------------------------------------
    public String displayTable() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s%-20s%10s   %10s%n", "SYM", "NAME", "PRICE", "CHANGE"));
        sb.append("-".repeat(52)).append("\n");
        for (String symbol : listSymbols()) {
            sb.append(stocks.get(symbol).toString()).append("\n");
        }
        return sb.toString();
    }
}
