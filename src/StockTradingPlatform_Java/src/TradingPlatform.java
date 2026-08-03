package StockTradingPlatform_Java.src;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class TradingPlatform {

    private Market market;
    private final Map<String, User> users;
    private User currentUser;

    public TradingPlatform() {
        Market loadedMarket = FileStorage.loadMarket();
        this.market = (loadedMarket != null) ? loadedMarket : new Market(true);

        Map<String, User> loadedUsers = FileStorage.loadUsers();
        this.users = (loadedUsers != null) ? loadedUsers : new LinkedHashMap<>();

        this.currentUser = null;
    }

    public void save() {
        FileStorage.saveMarket(market);
        FileStorage.saveUsers(users);
    }


    public User registerUser(String username, double startingCash) {
        username = username == null ? "" : username.trim();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("User '" + username + "' already exists.");
        }
        User user = new User(username, new Portfolio(startingCash));
        users.put(username, user);
        save();
        return user;
    }

    public User login(String username) {
        User user = users.get(username);
        if (user == null) {
            throw new IllegalArgumentException("No such user '" + username + "'. Register first.");
        }
        this.currentUser = user;
        return user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private User requireLogin() {
        if (currentUser == null) {
            throw new IllegalStateException("You must log in first.");
        }
        return currentUser;
    }

    public Transaction buy(String symbol, int quantity) {
        User user = requireLogin();
        Stock stock = market.getStock(symbol);
        if (stock == null) {
            throw new IllegalArgumentException("Unknown symbol '" + symbol + "'.");
        }
        Transaction txn = user.getPortfolio().buy(stock, quantity);
        user.getPortfolio().recordSnapshot(market);
        save();
        return txn;
    }

    public Transaction sell(String symbol, int quantity) {
        User user = requireLogin();
        Stock stock = market.getStock(symbol);
        if (stock == null) {
            throw new IllegalArgumentException("Unknown symbol '" + symbol + "'.");
        }
        Transaction txn = user.getPortfolio().sell(stock, quantity);
        user.getPortfolio().recordSnapshot(market);
        save();
        return txn;
    }

    public void simulateMarketTicks(int ticks) {
        for (int i = 0; i < ticks; i++) {
            market.tick();
        }
        if (currentUser != null) {
            currentUser.getPortfolio().recordSnapshot(market);
        }
        save();
    }

    public Market getMarket() {
        return market;
    }

    public Portfolio getPortfolio() {
        return requireLogin().getPortfolio();
    }

    public List<Portfolio.PositionReport> positionReports() {
        return requireLogin().getPortfolio().unrealizedGainLoss(market);
    }

    public List<Portfolio.Snapshot> performanceHistory() {
        return requireLogin().getPortfolio().getHistory();
    }

    public List<Transaction> transactionHistory() {
        return requireLogin().getPortfolio().getTransactions();
    }
}
