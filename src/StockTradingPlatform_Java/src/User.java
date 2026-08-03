package StockTradingPlatform_Java.src;

import java.io.Serializable;
import java.time.LocalDateTime;


public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final Portfolio portfolio;
    private final LocalDateTime createdAt;

    public User(String username, Portfolio portfolio) {
        this.username = username;
        this.portfolio = portfolio;
        this.createdAt = LocalDateTime.now();
    }

    public String getUsername() { return username; }
    public Portfolio getPortfolio() { return portfolio; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
