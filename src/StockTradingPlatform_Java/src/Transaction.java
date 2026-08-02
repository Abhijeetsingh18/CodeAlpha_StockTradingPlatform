package StockTradingPlatform_Java.src;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * An immutable record of a single BUY or SELL trade.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Action { BUY, SELL }

    private final String txnId;
    private final String symbol;
    private final Action action;
    private final int quantity;
    private final double price;
    private final double total;
    private final LocalDateTime timestamp;

    public Transaction(String symbol, Action action, int quantity, double price) {
        this.txnId = UUID.randomUUID().toString().substring(0, 8);
        this.symbol = symbol.toUpperCase();
        this.action = action;
        this.quantity = quantity;
        this.price = Math.round(price * 100.0) / 100.0;
        this.total = Math.round(quantity * this.price * 100.0) / 100.0;
        this.timestamp = LocalDateTime.now();
    }

    // ---- getters -----------------------------------------------------
    public String getTxnId() { return txnId; }
    public String getSymbol() { return symbol; }
    public Action getAction() { return action; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotal() { return total; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("[%s] %-4s %4d %-6s @ $%9.2f  = $%10.2f",
                timestamp.format(fmt), action, quantity, symbol, price, total);
    }
}
