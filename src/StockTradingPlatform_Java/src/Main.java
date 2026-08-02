package StockTradingPlatform_Java.src;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line interface for the Stock Trading Platform.
 * Run with: java Main
 */
public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String MENU = "\n"
            + "1) View Market Data\n"
            + "2) Register New User\n"
            + "3) Login\n"
            + "4) Buy Stock\n"
            + "5) Sell Stock\n"
            + "6) View Portfolio\n"
            + "7) View Performance Over Time\n"
            + "8) View Transaction History\n"
            + "9) Simulate Market Movement\n"
            + "0) Exit\n";

    public static void main(String[] args) {
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException ignored) {
            // fall back to platform default if UTF-8 somehow isn't available
        }
        TradingPlatform app = new TradingPlatform();
        printHeader("STOCK TRADING PLATFORM");
        System.out.println("Welcome! All data is persisted to the data/ folder.");

        boolean running = true;
        while (running) {
            String current = app.getCurrentUser() != null ? app.getCurrentUser().getUsername() : "Not logged in";
            System.out.println("\n[Current user: " + current + "]");
            System.out.println(MENU);
            System.out.print("Choose an option: ");
            String choice = SCANNER.nextLine().trim();

            switch (choice) {
                case "1": showMarket(app); break;
                case "2": doRegister(app); break;
                case "3": doLogin(app); break;
                case "4": doBuy(app); break;
                case "5": doSell(app); break;
                case "6": doPortfolio(app); break;
                case "7": doPerformance(app); break;
                case "8": doTransactions(app); break;
                case "9": doSimulate(app); break;
                case "0":
                    app.save();
                    System.out.println("Goodbye! Your data has been saved.");
                    running = false;
                    break;
                default:
                    System.out.println("\u274C Invalid option, try again.");
            }
        }
    }

    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(56));
        int pad = Math.max(0, (56 - title.length()) / 2);
        System.out.println(" ".repeat(pad) + title);
        System.out.println("=".repeat(56));
    }

    private static void showMarket(TradingPlatform app) {
        printHeader("MARKET DATA");
        System.out.print(app.getMarket().displayTable());
    }

    private static void doRegister(TradingPlatform app) {
        System.out.print("Choose a username: ");
        String username = SCANNER.nextLine().trim();
        System.out.print("Starting cash [default 10000]: ");
        String cashIn = SCANNER.nextLine().trim();
        try {
            double cash = cashIn.isEmpty() ? 10000.0 : Double.parseDouble(cashIn);
            app.registerUser(username, cash);
            System.out.printf("\u2705 User '%s' registered with $%,.2f starting cash.%n", username, cash);
        } catch (NumberFormatException e) {
            System.out.println("\u274C Invalid number entered for starting cash.");
        } catch (IllegalArgumentException e) {
            System.out.println("\u274C " + e.getMessage());
        }
    }

    private static void doLogin(TradingPlatform app) {
        System.out.print("Username: ");
        String username = SCANNER.nextLine().trim();
        try {
            app.login(username);
            System.out.println("\u2705 Logged in as " + username + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("\u274C " + e.getMessage());
        }
    }

    private static void doBuy(TradingPlatform app) {
        showMarket(app);
        System.out.print("\nSymbol to BUY: ");
        String symbol = SCANNER.nextLine().trim().toUpperCase();
        System.out.print("Quantity: ");
        String qtyIn = SCANNER.nextLine().trim();
        try {
            int qty = Integer.parseInt(qtyIn);
            Transaction txn = app.buy(symbol, qty);
            System.out.printf("\u2705 Bought %d %s @ $%.2f (total $%,.2f)%n",
                    txn.getQuantity(), txn.getSymbol(), txn.getPrice(), txn.getTotal());
        } catch (NumberFormatException e) {
            System.out.println("\u274C Quantity must be a whole number.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("\u274C " + e.getMessage());
        }
    }

    private static void doSell(TradingPlatform app) {
        showMarket(app);
        System.out.print("\nSymbol to SELL: ");
        String symbol = SCANNER.nextLine().trim().toUpperCase();
        System.out.print("Quantity: ");
        String qtyIn = SCANNER.nextLine().trim();
        try {
            int qty = Integer.parseInt(qtyIn);
            Transaction txn = app.sell(symbol, qty);
            System.out.printf("\u2705 Sold %d %s @ $%.2f (total $%,.2f)%n",
                    txn.getQuantity(), txn.getSymbol(), txn.getPrice(), txn.getTotal());
        } catch (NumberFormatException e) {
            System.out.println("\u274C Quantity must be a whole number.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("\u274C " + e.getMessage());
        }
    }

    private static void doPortfolio(TradingPlatform app) {
        Portfolio portfolio;
        try {
            portfolio = app.getPortfolio();
        } catch (IllegalStateException e) {
            System.out.println("\u274C " + e.getMessage());
            return;
        }
        printHeader("PORTFOLIO \u2014 " + app.getCurrentUser().getUsername());
        System.out.printf("Cash Balance:     $%,12.2f%n", portfolio.getCashBalance());
        System.out.printf("Holdings Value:   $%,12.2f%n", portfolio.holdingsValue(app.getMarket()));
        System.out.printf("TOTAL VALUE:      $%,12.2f%n", portfolio.totalValue(app.getMarket()));

        List<Portfolio.PositionReport> positions = app.positionReports();
        if (!positions.isEmpty()) {
            System.out.println();
            System.out.printf("%-6s%6s%12s%12s%14s%20s%n", "SYM", "QTY", "AVG COST", "PRICE", "VALUE", "P/L");
            System.out.println("-".repeat(70));
            for (Portfolio.PositionReport pos : positions) {
                String plStr = String.format("$%+,.2f (%+.2f%%)", pos.gainLoss, pos.gainLossPct);
                System.out.printf("%-6s%6d%12.2f%12.2f%,14.2f%20s%n",
                        pos.symbol, pos.quantity, pos.avgCost, pos.currentPrice, pos.marketValue, plStr);
            }
        } else {
            System.out.println("\n(No open positions yet \u2014 try buying a stock!)");
        }
    }

    private static void doPerformance(TradingPlatform app) {
        List<Portfolio.Snapshot> history;
        try {
            history = app.performanceHistory();
        } catch (IllegalStateException e) {
            System.out.println("\u274C " + e.getMessage());
            return;
        }
        printHeader("PORTFOLIO PERFORMANCE OVER TIME");
        if (history.isEmpty()) {
            System.out.println("No snapshots yet. Buy/sell something or simulate the market to generate history.");
            return;
        }
        System.out.printf("%-22s%12s%12s%14s%n", "TIMESTAMP", "CASH", "HOLDINGS", "TOTAL");
        System.out.println("-".repeat(60));
        int start = Math.max(0, history.size() - 25);
        for (int i = start; i < history.size(); i++) {
            Portfolio.Snapshot snap = history.get(i);
            System.out.printf("%-22s%,12.2f%,12.2f%,14.2f%n",
                    snap.timestamp.format(TS_FMT), snap.cash, snap.holdingsValue, snap.totalValue);
        }
        double first = history.get(0).totalValue;
        double last = history.get(history.size() - 1).totalValue;
        double change = last - first;
        double pct = first != 0 ? (change / first) * 100 : 0;
        System.out.println("-".repeat(60));
        System.out.printf("Change since first snapshot: $%+,.2f (%+.2f%%)%n", change, pct);
    }

    private static void doTransactions(TradingPlatform app) {
        List<Transaction> txns;
        try {
            txns = app.transactionHistory();
        } catch (IllegalStateException e) {
            System.out.println("\u274C " + e.getMessage());
            return;
        }
        printHeader("TRANSACTION HISTORY");
        if (txns.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction t : txns) {
            System.out.println(t);
        }
    }

    private static void doSimulate(TradingPlatform app) {
        System.out.print("How many market ticks to simulate? [default 1]: ");
        String ticksIn = SCANNER.nextLine().trim();
        int ticks = ticksIn.isEmpty() ? 1 : Integer.parseInt(ticksIn);
        app.simulateMarketTicks(ticks);
        System.out.printf("\u2705 Simulated %d market tick(s). Prices updated.%n", ticks);
        showMarket(app);
    }
}
