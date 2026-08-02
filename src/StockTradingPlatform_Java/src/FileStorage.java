package StockTradingPlatform_Java.src;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple binary file persistence for the trading platform, using Java's
 * built-in object serialization. Keeps market data and all user accounts
 * / portfolios on disk (data/market.dat, data/users.dat) so state survives
 * between runs.
 */
public class FileStorage {
    private static final String DATA_DIR = "data";
    private static final String MARKET_FILE = DATA_DIR + File.separator + "market.dat";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.dat";

    private static void ensureDataDir() throws IOException {
        Path dir = Paths.get(DATA_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    public static void saveMarket(Market market) {
        try {
            ensureDataDir();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(MARKET_FILE))) {
                out.writeObject(market);
            }
        } catch (IOException e) {
            System.err.println("Warning: failed to save market data — " + e.getMessage());
        }
    }

    public static Market loadMarket() {
        File file = new File(MARKET_FILE);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Market) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: failed to load market data — " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveUsers(Map<String, User> users) {
        try {
            ensureDataDir();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
                out.writeObject(users);
            }
        } catch (IOException e) {
            System.err.println("Warning: failed to save user data — " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new LinkedHashMap<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, User>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: failed to load user data — " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }
}
