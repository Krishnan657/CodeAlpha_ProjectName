import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * StockTradingPlatform.java
 *
 * Simple console-based stock trading simulation.
 *
 * Features:
 * - Preloaded market stocks with prices
 * - View market data
 * - Buy / Sell operations (updates portfolio & cash)
 * - View portfolio and transaction history
 * - Simple market price simulation (random small drift)
 * - Save / Load portfolio to/from CSV file (portfolio.csv)
 *
 * Compile: javac StockTradingPlatform.java
 * Run:     java StockTradingPlatform
 */
public class StockTradingPlatform {

    static class Stock {
        String symbol;
        String name;
        double price; // current market price

        Stock(String symbol, String name, double price) {
            this.symbol = symbol;
            this.name = name;
            this.price = price;
        }
    }

    static class Transaction {
        LocalDateTime timestamp;
        String symbol;
        int qty; // positive buy, negative sell
        double price; // executed price
        String type; // "BUY" or "SELL"

        Transaction(LocalDateTime t, String symbol, int qty, double price, String type) {
            this.timestamp = t;
            this.symbol = symbol;
            this.qty = qty;
            this.price = price;
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("%s | %s %d @ %.2f | %s",
                    timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    type, Math.abs(qty), price, symbol);
        }

        String toCsv() {
            return String.format("%s,%s,%d,%.2f,%s",
                    timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    symbol, qty, price, type);
        }
    }

    // Portfolio: holdings map symbol -> quantity, and cash balance
    static class Portfolio {
        Map<String, Integer> holdings = new HashMap<>();
        double cash;

        Portfolio(double startingCash) {
            this.cash = startingCash;
        }

        int getQty(String symbol) {
            return holdings.getOrDefault(symbol, 0);
        }

        void changeQty(String symbol, int delta) {
            holdings.put(symbol, holdings.getOrDefault(symbol, 0) + delta);
            if (holdings.get(symbol) == 0) holdings.remove(symbol);
        }
    }

    private final Scanner sc = new Scanner(System.in);
    private final Map<String, Stock> market = new LinkedHashMap<>();
    private final Portfolio portfolio = new Portfolio(10000.0); // start with ₹10,000 (or any currency)
    private final List<Transaction> history = new ArrayList<>();
    private final Random rnd = new Random();

    public static void main(String[] args) {
        StockTradingPlatform app = new StockTradingPlatform();
        app.setupMarket();
        app.loadPortfolioFromCsvIfExists(); // optional persistence
        app.run();
    }

    private void setupMarket() {
        // Some example stocks
        market.put("INFY", new Stock("INFY", "Infosys Ltd", 1400.00));
        market.put("TCS", new Stock("TCS", "Tata Consultancy", 3200.00));
        market.put("RELI", new Stock("RELI", "Reliance Industries", 2900.00));
        market.put("HDFC", new Stock("HDFC", "HDFC Bank", 1500.00));
        market.put("ICIC", new Stock("ICIC", "ICICI Bank", 1000.00));
    }

    private void run() {
        System.out.println("=== Simple Stock Trading Platform ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> viewMarket();
                case "2" -> buy();
                case "3" -> sell();
                case "4" -> viewPortfolio();
                case "5" -> viewHistory();
                case "6" -> simulateMarketMovement();
                case "7" -> {
                    savePortfolioToCsv();
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
        System.out.println("Exiting. Portfolio saved to portfolio.csv. Goodbye!");
    }

    private void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. View market data");
        System.out.println("2. Buy stock");
        System.out.println("3. Sell stock");
        System.out.println("4. View portfolio & cash balance");
        System.out.println("5. View transaction history");
        System.out.println("6. Simulate market price movement");
        System.out.println("7. Save & Exit");
        System.out.print("Choose: ");
    }

    private void viewMarket() {
        System.out.println("\n-- Market --");
        for (Stock s : market.values()) {
            System.out.printf("%s | %s | Price: %.2f%n", s.symbol, s.name, s.price);
        }
        System.out.println("(Tip: use Simulate market price movement to change prices.)");
    }

    private void buy() {
        System.out.print("Enter symbol to BUY: ");
        String sym = sc.nextLine().trim().toUpperCase();
        Stock s = market.get(sym);
        if (s == null) {
            System.out.println("Unknown symbol.");
            return;
        }
        System.out.print("Quantity to buy (integer): ");
        int qty;
        try {
            qty = Integer.parseInt(sc.nextLine().trim());
            if (qty <= 0) {
                System.out.println("Quantity must be > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }
        double cost = qty * s.price;
        if (cost > portfolio.cash) {
            System.out.printf("Insufficient cash. Need %.2f but have %.2f%n", cost, portfolio.cash);
            return;
        }
        portfolio.cash -= cost;
        portfolio.changeQty(sym, qty);
        Transaction t = new Transaction(LocalDateTime.now(), sym, qty, s.price, "BUY");
        history.add(t);
        System.out.printf("Bought %d %s @ %.2f. New cash: %.2f%n", qty, sym, s.price, portfolio.cash);
    }

    private void sell() {
        System.out.print("Enter symbol to SELL: ");
        String sym = sc.nextLine().trim().toUpperCase();
        Stock s = market.get(sym);
        if (s == null) {
            System.out.println("Unknown symbol.");
            return;
        }
        int owned = portfolio.getQty(sym);
        if (owned == 0) {
            System.out.println("You don't own any shares of " + sym);
            return;
        }
        System.out.print("Quantity to sell (integer): ");
        int qty;
        try {
            qty = Integer.parseInt(sc.nextLine().trim());
            if (qty <= 0) {
                System.out.println("Quantity must be > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }
        if (qty > owned) {
            System.out.println("Cannot sell more than you own. Owned: " + owned);
            return;
        }
        double proceeds = qty * s.price;
        portfolio.cash += proceeds;
        portfolio.changeQty(sym, -qty);
        Transaction t = new Transaction(LocalDateTime.now(), sym, -qty, s.price, "SELL");
        history.add(t);
        System.out.printf("Sold %d %s @ %.2f. New cash: %.2f%n", qty, sym, s.price, portfolio.cash);
    }

    private void viewPortfolio() {
        System.out.println("\n-- Portfolio --");
        System.out.printf("Cash: %.2f%n", portfolio.cash);
        if (portfolio.holdings.isEmpty()) {
            System.out.println("No holdings.");
        } else {
            double totalValue = portfolio.cash;
            System.out.println("Holdings:");
            for (Map.Entry<String, Integer> e : portfolio.holdings.entrySet()) {
                String sym = e.getKey();
                int qty = e.getValue();
                Stock s = market.get(sym);
                double val = (s != null) ? qty * s.price : 0.0;
                totalValue += val;
                System.out.printf("%s: %d shares | Price: %.2f | Value: %.2f%n", sym, qty,
                        (s != null) ? s.price : 0.0, val);
            }
            System.out.printf("Total portfolio value (cash + holdings): %.2f%n", totalValue);
        }
    }

    private void viewHistory() {
        System.out.println("\n-- Transaction History --");
        if (history.isEmpty()) {
            System.out.println("No transactions.");
            return;
        }
        for (Transaction t : history) {
            System.out.println(t);
        }
    }

    // small random simulation of price movement; +/- up to 5%
    private void simulateMarketMovement() {
        System.out.println("Simulating market movement...");
        for (Stock s : market.values()) {
            double pct = (rnd.nextDouble() * 0.10) - 0.05; // -5% to +5%
            s.price = Math.max(1.0, s.price * (1.0 + pct));
        }
        System.out.println("Market updated.");
        viewMarket();
    }

    // Persistence: save portfolio and transaction history to CSV
    private void savePortfolioToCsv() {
        File f = new File("portfolio.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            // header
            pw.println("cash," + portfolio.cash);
            // holdings
            pw.println("holdings");
            for (Map.Entry<String, Integer> e : portfolio.holdings.entrySet()) {
                pw.println(e.getKey() + "," + e.getValue());
            }
            // transactions
            pw.println("transactions");
            for (Transaction t : history) {
                pw.println(t.toCsv());
            }
            System.out.println("Saved portfolio to " + f.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to save portfolio: " + e.getMessage());
        }
    }

    // Try to load portfolio.csv if present
    private void loadPortfolioFromCsvIfExists() {
        File f = new File("portfolio.csv");
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            boolean inHoldings = false;
            boolean inTransactions = false;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                if (line.startsWith("cash,")) {
                    String[] parts = line.split(",", 2);
                    portfolio.cash = Double.parseDouble(parts[1]);
                    continue;
                }
                if (line.equalsIgnoreCase("holdings")) {
                    inHoldings = true;
                    inTransactions = false;
                    continue;
                }
                if (line.equalsIgnoreCase("transactions")) {
                    inHoldings = false;
                    inTransactions = true;
                    continue;
                }
                if (inHoldings) {
                    String[] p = line.split(",", 2);
                    if (p.length == 2) {
                        portfolio.holdings.put(p[0], Integer.parseInt(p[1]));
                    }
                } else if (inTransactions) {
                    // timestamp,symbol,qty,price,type
                    String[] p = line.split(",", 5);
                    if (p.length == 5) {
                        LocalDateTime t = LocalDateTime.parse(p[0]);
                        String symbol = p[1];
                        int qty = Integer.parseInt(p[2]);
                        double price = Double.parseDouble(p[3]);
                        String type = p[4];
                        history.add(new Transaction(t, symbol, qty, price, type));
                    }
                }
            }
            System.out.println("Loaded portfolio from portfolio.csv (cash and holdings).");
        } catch (Exception e) {
            System.out.println("Failed to load portfolio.csv: " + e.getMessage());
        }
    }
}
