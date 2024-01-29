import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class UserInterface {
    private Scanner scanner = new Scanner(System.in);

    public void init(Connection connection) throws SQLException {
        System.out.println("Welcome to StarsRUs!");

        while (true) {
            System.out.println("\n--- StarsRUs ---");
            System.out.println("Choose an option:");
            System.out.println("1. Login");
            System.out.println("2. Create an Account");
            System.out.println("3. Exit");

            int choice = getUserChoice();
            switch (choice) {
                case 1:
                    login(connection);
                    break;
                case 2:
                    createAccount(connection);
                    break;
                case 3:
                    System.out.println("Exiting StarsRUs. Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            break;
        }
    }

    public int getUserChoice() {
        System.out.print("Enter your choice: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    public void login(Connection connection) throws SQLException {
        System.out.println("\n--- Login ---");
        System.out.print("Enter username: ");
        String username = scanner.next();
        System.out.print("Enter password: ");
        String password = scanner.next();
        
        Customer customer = new Customer();
        customer.login(connection, username, password);
    }

    public void createAccount(Connection connection) throws SQLException {
        System.out.println("\n--- Create an Account ---");
        System.out.print("Enter name: ");
        scanner.nextLine();
        String name = scanner.nextLine();
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter state: ");
        String state = scanner.next();
        System.out.print("Enter phone number (Ex:(805)2574499): ");
        String phone = scanner.next();
        System.out.print("Enter email: ");
        String email = scanner.next();

        String max_id = "";
        try (PreparedStatement readyStatement = connection.prepareStatement("SELECT MAX(c_taxid) FROM CUSTOMERS")) {
            try (ResultSet readyResultSet = readyStatement.executeQuery()) {
                if (readyResultSet.next()) {
                    max_id = readyResultSet.getString(1);
                }
            }
        }
        int numericId = Integer.parseInt(max_id);
        numericId++;
        max_id = String.format("%09d", numericId);

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO CUSTOMERS VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, state);
            preparedStatement.setString(5, phone);
            preparedStatement.setString(6, email);
            preparedStatement.setString(7, max_id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Insertion successful!");
                System.out.println("Account created successfully for user: " + username);
                pay(username, connection);
            } else {
                System.out.println("Customer insertion failed.");
            }
        } catch (Exception e) {
            System.out.println("ERROR: insertion failed.");
            System.out.println(e);
        }           
    }

    public void pay(String username, Connection connection) throws SQLException {
        double balance = 0.0;
        while (balance < 1000) {
            System.out.println("\nPlease make an initial deposit of at least $1,000 into your account.");
            System.out.println("Insert the amount of $ you would like to add to your account: ");
            balance = scanner.nextDouble();
            if (balance < 1000) {
                System.out.println("\nThe deposit must be at least $1,000. Please try again.");
            }
        }
        System.out.println("\nThank you for your deposit of $" + balance);

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String dateString = resultSet.getString("curr_day");
                    LocalDateTime date = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDateTime compareDate = LocalDateTime.of(date.getYear(), date.getMonth(), 1, date.getHour(), date.getMinute(), date.getSecond());
                    while (date.getDayOfMonth() != compareDate.getDayOfMonth()) {
                        String dateString2 = compareDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        dateString2 = dateString2.substring(0, 10);
                        try (PreparedStatement stocksStatement = connection.prepareStatement("INSERT INTO Daily_Bal VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'))")) {
                            stocksStatement.setString(1, username);
                            stocksStatement.setDouble(2, balance);
                            stocksStatement.setString(3, dateString2);
                            int rowsAffected = stocksStatement.executeUpdate();
                            if (rowsAffected > 0) {
                            } else {
                                System.out.println("Daily_Bal insertion failed.");
                            }
                        }
                        compareDate = compareDate.plusDays(1);
                    }
                }
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO MARKET_ACCOUNTS (c_username, market_acc_balance, shares_traded, DTER, initial_bal, interest_prev) VALUES (?, ?, 0, 0, ?, 0)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, balance);
            preparedStatement.setDouble(3, balance);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Market_Account insertion successful!");
                traderInterace(username, connection);
            } else {
                System.out.println("Market_Account insertion failed.");
            }
        } catch (Exception e) {
            System.out.println("ERROR: insertion failed.");
            System.out.println(e);
        }
    }

    public void openMarketFirstDayOfMonth(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Day SET curr_day = curr_day + 1 WHERE day_id = 1")) {
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\nOpened market for new day!");
            } else {
                System.out.println("Open market did not work.");
            }
        }                   

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Stock_Shares_Accounts WHERE stock_shares = 0")) {
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Removed stock shares accounts with 0 stocks");
            } 
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String dateString = resultSet.getString("curr_day");
                    LocalDateTime date = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if (date.getDayOfMonth() == 1) {
                        try (PreparedStatement marketStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET shares_traded = 0")) {
                            int rowsAffected = marketStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("Shares traded during month reset.");
                            } else {
                                System.out.println("Shares traded did not reset to 0.");
                            }
                        }
                                    
                        try (PreparedStatement latestStatement = connection.prepareStatement("DELETE FROM Latest_Transaction")) {
                            int rowsAffected = latestStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("Latest_Transaction rows deleted successfully ");
                            } else {
                                System.out.println("Latest_Transaction rows were not deleted.");
                            }
                        }

                        try (PreparedStatement resetStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET DTER = 0")) {
                            int rowsAffected = resetStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("DTER set to 0.");
                            } else {
                                System.out.println("DTER was not able to be changed.");
                            }
                        }
                                    
                        String guy = "";
                        try (PreparedStatement personStatement = connection.prepareStatement("SELECT c_username FROM MARKET_ACCOUNTS")) {
                            try (ResultSet personResultSet = personStatement.executeQuery()) {
                                while (personResultSet.next()) {
                                    guy = personResultSet.getString("c_username");
                                    int commissions = 0;
                                    double total = 0.0;
                                    double interest_prev = 0.0;
                                    try (PreparedStatement readyStatement = connection.prepareStatement("SELECT * FROM TRANSACTIONS WHERE c_username = ?")) {
                                        readyStatement.setString(1, guy);
                                        try (ResultSet readyResultSet = readyStatement.executeQuery()) {
                                            while (readyResultSet.next()) {
                                                String trans_type = readyResultSet.getString("trans_type");
                                                if (trans_type.equals("buy") || trans_type.equals("sell")) {
                                                    double value = readyResultSet.getDouble("value");
                                                    total = total + value;
                                                    commissions = commissions + 20;
                                                }
                                                else {
                                                    double value = readyResultSet.getDouble("value");
                                                    if (trans_type.equals("cancel")) {
                                                        commissions = commissions + 20;
                                                        total = total + value;
                                                    }
                                                }
                                            }
                                        } 
                                    }

                                    try (PreparedStatement readyStatement = connection.prepareStatement("SELECT * FROM MARKET_ACCOUNTS WHERE c_username = ?")) {
                                        readyStatement.setString(1, guy);
                                        try (ResultSet readyResultSet = readyStatement.executeQuery()) {
                                            if (readyResultSet.next()) {
                                                interest_prev = readyResultSet.getDouble("interest_prev");
                                            }
                                        }
                                    }

                                    total = total - commissions + interest_prev;
                                    if (total > 10000) {
                                        try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET DTER = 1 WHERE c_username = ?")) {
                                            updateStatement.setString(1, guy);
                                            int rowsAffected = updateStatement.executeUpdate();
                                            if (rowsAffected > 0) {
                                                System.out.println("DTER set to 1.");
                                            } else {
                                                System.out.println("DTER was not able to be changed.");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                                    
                        try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET initial_bal = market_acc_balance")) {
                            int rowsAffected = updateStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("Market initial balance updated.");
                            } else {
                                System.out.println("Market initial balance was not able to be changed.");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("ERROR: insertion failed.");
                System.out.println(e);
            }
        }
    }

    public void managerInterace(String username, Connection connection) throws SQLException {
        System.out.println("\nWelcome to the Manager Interface!");
        MarketAccount marketAccount = new MarketAccount();
        Transaction transaction = new Transaction();
        Customer customer = new Customer();
        String name;
        String today = "";

        while (true) {
            System.out.println("\n--- Manager Interface ---");
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        today = resultSet.getString("curr_day");
                        today = today.substring(0, 10);
                        System.out.println("Today's date: " + today);
                    }
                }
            }
            System.out.println("Choose an option:");
            System.out.println("1. Add Interest to Accounts (Auto-Opens Market for next day)");
            System.out.println("2. Generate Monthly Statement");
            System.out.println("3. List Active Customers");
            System.out.println("4. Generate Government Drug & Tax Evasion Report");
            System.out.println("5. Customer Report");
            System.out.println("6. Delete Transactions");
            System.out.println("7. Open Market for the day");
            System.out.println("8. Close Market for the day");
            System.out.println("9. Set a new price for a stock");
            System.out.println("10. Set a new date to be today's date and Open Market");
            System.out.println("11. Exit");

            int choice = getUserChoice();
            switch (choice) {
                case 1:
                    try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                String dateString = resultSet.getString("curr_day");
                                LocalDateTime date = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                dateString = dateString.substring(0, 10);
                                if (date.getDayOfMonth() == date.getMonth().maxLength()) {
                                    System.out.println("\n" + dateString + " is the last day of the month.");
                                    try (PreparedStatement balStatement = connection.prepareStatement("SELECT * FROM MARKET_ACCOUNTS")) {
                                        try (ResultSet balResultSet = balStatement.executeQuery()) {
                                            while (balResultSet.next()) {
                                                String thatguy = balResultSet.getString("c_username");
                                                double marketbal = balResultSet.getDouble("market_acc_balance");
                                                try (PreparedStatement stocksStatement = connection.prepareStatement("INSERT INTO Daily_Bal VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'))")) {
                                                    stocksStatement.setString(1, thatguy);
                                                    stocksStatement.setDouble(2, marketbal);
                                                    stocksStatement.setString(3, today);
                                                    int rowsAffected = stocksStatement.executeUpdate();
                                                    if (rowsAffected > 0) {
                                                    } else {
                                                        System.out.println("Daily_Bal insertion failed.");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    marketAccount.addInterest(connection, date);
                                    openMarketFirstDayOfMonth(connection);
                                    try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM Daily_Bal")) {
                                        try (ResultSet deleteResultSet = deleteStatement.executeQuery()) {
                                            System.out.println("\nAll daily_bal deleted from each accounts");
                                        }
                                    } 
                                } else {
                                    System.out.println("\n" + dateString + " is not the last day of the month.");
                                    System.out.println("Cannot add interests.");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("ERROR: insertion failed.");
                            System.out.println(e);
                        }
                    }
                    break;
                case 2:
                    System.out.println("\nWhich customer's monthly statement would you like to generate?");
                    name = scanner.next();
                    transaction.generateMonthlyStatement(connection, name);
                    break;
                case 3:
                    transaction.listActiveCustomers(connection);
                    break;
                case 4:
                    transaction.listDTER(connection);
                    break;
                case 5:
                    System.out.println("\nWhich customer's report would you like to generate?");
                    name = scanner.next();
                    customer.customerReport(connection, name);
                    break;
                case 6:
                    try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Transactions")) {
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            System.out.println("\nAll transactions deleted from each accounts");
                        }
                    }                    
                    break;
                case 7:
                    try (PreparedStatement balStatement = connection.prepareStatement("SELECT * FROM MARKET_ACCOUNTS")) {
                        try (ResultSet balResultSet = balStatement.executeQuery()) {
                            while (balResultSet.next()) {
                                String thatguy = balResultSet.getString("c_username");
                                double marketbal = balResultSet.getDouble("market_acc_balance");
                                try (PreparedStatement stocksStatement = connection.prepareStatement("INSERT INTO Daily_Bal VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'))")) {
                                    stocksStatement.setString(1, thatguy);
                                    stocksStatement.setDouble(2, marketbal);
                                    stocksStatement.setString(3, today);
                                    int rowsAffected = stocksStatement.executeUpdate();
                                    if (rowsAffected > 0) {
                                    } else {
                                        System.out.println("Daily_Bal insertion failed.");
                                    }
                                }
                            }
                        }
                    }

                    try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Day SET curr_day = curr_day + 1 WHERE day_id = 1")) {
                    
                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("\nOpened market for new day!");
                        } else {
                            System.out.println("Open market did not work.");
                        }
                    }                   

                    try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Stock_Shares_Accounts WHERE stock_shares = 0")) {
                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Removed stock shares accounts with 0 stocks");
                        } 
                    }

                    try (PreparedStatement latestStatement = connection.prepareStatement("DELETE FROM Latest_Transaction")) {
                        int rowsAffected = latestStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Latest_Transaction rows deleted successfully ");
                        } else {
                            System.out.println("Latest_Transaction rows were not deleted.");
                        }
                    }
                    break;
                case 8:
                    try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                String day = resultSet.getString("curr_day");
                                day = day.substring(0, 10);
                                try (PreparedStatement actorsStatement = connection.prepareStatement("SELECT * FROM ACTORS")) {  
                                    try (ResultSet actorsResultSet = actorsStatement.executeQuery()) {
                                        while (actorsResultSet.next()) {
                                            String stock_symbol = actorsResultSet.getString("stock_symbol");
                                            double curr_price = actorsResultSet.getDouble("curr_price");
                                            try (PreparedStatement stocksStatement = connection.prepareStatement("INSERT INTO Stocks VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'))")) {
                                                stocksStatement.setString(1, stock_symbol);
                                                stocksStatement.setDouble(2, curr_price);
                                                stocksStatement.setString(3, day);

                                                int rowsAffected = stocksStatement.executeUpdate();
                                                if (rowsAffected > 0) {
                                                    System.out.println("Market Close insertion created successfully: ");
                                                } else {
                                                    System.out.println("Market Close insertion failed.");
                                                }
                                            }                       
                                        }
                                    }
                                }
                            }
                        }
                    }
                    System.out.println("\nMarket has been closed.");
                    break;
                case 9:
                    System.out.println("Which stock would you like to set a new price for?");
                    String stock_symbol = scanner.next();
                    System.out.println("What would you like the new price of " + stock_symbol + " stock to be?");
                    double currprice = scanner.nextDouble();
                    try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE ACTORS SET curr_price = ? WHERE stock_symbol = ?")) {
                        preparedStatement.setDouble(1, currprice);
                        preparedStatement.setString(2, stock_symbol);
                    
                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("New " + stock_symbol + " stock price: $" + currprice);
                        } else {
                            System.out.println("Stock price update did not work for: " + stock_symbol);
                        }
                    } catch (SQLException e) {
                        System.out.println("Error updating stock price.");
                        System.out.println(e);
                    }
                    break;
                case 10:
                    System.out.println("\nWhat would you like to set the date to? (Format: YYYY-MM-DD)");
                    String day = scanner.next();
                    String old_day = "";
                    try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                old_day = resultSet.getString("curr_day");
                            }
                        }
                    }

                    try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Day SET curr_day = TO_DATE(?, 'YYYY-MM-DD') WHERE day_id = 1")) {
                        preparedStatement.setString(1, day);
                    
                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("\nNew date: " + day);
                        } else {
                            System.out.println("Date update did not work.");
                        }
                    }

                    try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Stock_Shares_Accounts WHERE stock_shares = 0")) {
                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Removed stock shares accounts with 0 stocks");
                        } 
                    }

                    try (PreparedStatement latestStatement = connection.prepareStatement("DELETE FROM Latest_Transaction")) {
                        int rowsAffected = latestStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Latest_Transaction rows deleted successfully ");
                        } else {
                            System.out.println("Latest_Transaction rows were not deleted.");
                        }
                    }

                    LocalDateTime date = LocalDateTime.parse(old_day, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDateTime compareDate = date;
                    LocalDateTime date2 = LocalDateTime.parse(day + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    boolean updateStocks = false;

                    try (PreparedStatement balStatement = connection.prepareStatement("SELECT * FROM MARKET_ACCOUNTS")) {
                        try (ResultSet balResultSet = balStatement.executeQuery()) {
                            while (balResultSet.next()) {
                                String thatguy = balResultSet.getString("c_username");
                                double marketbal = balResultSet.getDouble("market_acc_balance");
                                while (! compareDate.equals(date2)) {
                                    String current_day = compareDate.toString().substring(0,10);
                                    try (PreparedStatement stocksStatement = connection.prepareStatement("INSERT INTO Daily_Bal VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'))")) {
                                        stocksStatement.setString(1, thatguy);
                                        stocksStatement.setDouble(2, marketbal);
                                        stocksStatement.setString(3, current_day);
                                        compareDate = compareDate.plusDays(1);
                                        int rowsAffected = stocksStatement.executeUpdate();
                                        if (rowsAffected > 0) {
                                        } else {
                                            System.out.println("Daily_Bal insertion failed.");
                                        }
                                    }
                                    if (!updateStocks) {
                                        try (PreparedStatement actorsStatement = connection.prepareStatement("SELECT * FROM ACTORS")) {  
                                            try (ResultSet actorsResultSet = actorsStatement.executeQuery()) {
                                                while (actorsResultSet.next()) {
                                                    String stock_symbols = actorsResultSet.getString("stock_symbol");
                                                    double curr_price = actorsResultSet.getDouble("curr_price");
                                                    try (PreparedStatement stocksStatement = connection.prepareStatement("INSERT INTO Stocks VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'))")) {
                                                        stocksStatement.setString(1, stock_symbols);
                                                        stocksStatement.setDouble(2, curr_price);
                                                        stocksStatement.setString(3, current_day);

                                                        int rowsAffected = stocksStatement.executeUpdate();
                                                        if (rowsAffected > 0) {
                                                        } else {
                                                            System.out.println("Market Close insertion failed.");
                                                        }
                                                    }                       
                                                }
                                            }
                                        }
                                    }
                                }
                                compareDate = date;
                                updateStocks = true;
                            }
                        }
                    }
                    break;
                case 11:
                    System.out.println("Exiting StarsRUs. Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public void traderInterace(String username, Connection connection) throws SQLException {
        System.out.println("\nWelcome to the Trading Interface!");
        StockAccount stockAccount = new StockAccount();
        MarketAccount marketAccount = new MarketAccount();
        Transaction transaction = new Transaction();
        Movie movie = new Movie();
        Actor actor = new Actor();
        String stock_symbol;
        double balance = 0.00;
        double stock_shares = 0.0;
        String today = "";

        while (true) {
            System.out.println("\n--- Trading Interface ---");
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        today = resultSet.getString("curr_day");
                        today = today.substring(0, 10);
                        System.out.println("Today's date: " + today);
                    }
                }
            }
            System.out.println("Choose an option:");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Buy Stocks");
            System.out.println("4. Sell Stocks");
            System.out.println("5. Cancel last buy/sell ($20 commission fee)");
            System.out.println("6. View balance");
            System.out.println("7. View Transaction History");
            System.out.println("8. List All Stocks");
            System.out.println("9. List All Movie Information");
            System.out.println("10. Exit");

            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    System.out.println("Insert the amount of $ you would like to add to your account: ");
                    balance = scanner.nextDouble();
                    marketAccount.deposit(connection, username, balance);
                    break;
                case 2:
                    System.out.println("Insert the amount of $ you would like to withdraw from your account: ");
                    double balance3 = scanner.nextDouble();
                    balance = marketAccount.checkBal2(connection, username);
                    if (balance - balance3 >= 0) {
                        marketAccount.withdraw(connection, username, balance3);
                    }
                    else {
                        System.out.println("Error: Cannot withdraw more money than owned.");
                    }
                    break;
                case 3:
                    System.out.println("Which stock would you like to buy?");
                    stock_symbol = scanner.next();
                    stockAccount.checkStockAccountExist(connection, username, stock_symbol);

                    System.out.println("How many " + stock_symbol + " stock would you like to buy? $20 commission fee");
                    stock_shares = scanner.nextDouble();
                    stockAccount.buyStocks(connection, username, stock_symbol, stock_shares);
                    break;
                case 4:
                    System.out.println("Which stock would you like to sell?");
                    stock_symbol = scanner.next();
                    stockAccount.checkStockAccountExist(connection, username, stock_symbol);

                    System.out.println("\nWhich " + stock_symbol + " stock would you like to sell? (Type the ID or Type \"exit\") $20 commission fee");
                    stockAccount.listSpecificStocks(connection, username, stock_symbol);
                    String id = scanner.next();
                    double stock_shares2 = 0.0;
                    double total_stock_shares = 0.0;
                    boolean stocks_sold = false;
                    if (! id.equals("exit")) {
                        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Latest_Transaction WHERE c_username = ?")) {
                            preparedStatement.setString(1, username);
                
                            int rowsAffected = preparedStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("Latest_Transaction rows deleted successfully ");
                            } else {
                                System.out.println("Latest_Transaction rows were not deleted.");
                            }
                        }
                        while (! id.equals("exit")) {
                            System.out.println("\nHow many of that specific " + stock_symbol + " stock would you like to sell?");
                            stock_shares2 = scanner.nextDouble();
                            total_stock_shares = total_stock_shares + stockAccount.sellStockShares(connection, username, stock_symbol, stock_shares2, id);
                            stocks_sold = true;
                            System.out.println("\nWhich " + stock_symbol + " stock would you like to sell? (Type the ID or Type \"exit\")");
                            stockAccount.listSpecificStocks(connection, username, stock_symbol);
                            id = scanner.next();
                        }
                        if (stocks_sold) {
                            stockAccount.sellStocks(connection, username, stock_symbol, total_stock_shares);
                            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT market_acc_balance FROM MARKET_ACCOUNTS WHERE c_username = ?")) {
                                preparedStatement.setString(1, username);
                                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                    if (resultSet.next()) {
                                        balance = resultSet.getDouble("market_acc_balance");
                                    }
                                } 
                            }

                            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET market_acc_balance = market_acc_balance - 20 WHERE c_username = ?")) {
                                preparedStatement.setString(1, username);
                                balance = balance - 20;
                                int rowsAffected = preparedStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    System.out.println("New market account balance: $" + balance);
                                } else {
                                    System.out.println("Market account balance update did not work for user: " + username);
                                }
                            } catch (SQLException e) {
                                System.out.println("Error updating user data.");
                                System.out.println(e);
                            }
                        }
                    }
                    break;
                case 5:
                    transaction.cancelLatestTransaction(connection, username);
                    break;
                case 6:       
                    marketAccount.checkBal(connection, username, balance);
                    break;
                case 7:
                    transaction.getTransactionList(connection, username);
                    break;
                case 8:
                    actor.listStocks(connection);
                    break;
                case 9:
                    movie.displayMovies(connection);
                    int choice2 = 0;
                    while (choice2 != 3) {
                        System.out.println("\nChoose an option:");
                        System.out.println("1. See Top Movies in a given year frame");
                        System.out.println("2. See reviews for a movie");
                        System.out.println("3. Go back to Trading Interface");

                        choice2 = getUserChoice();
                        switch (choice2) {
                            case 1:
                                System.out.println("Please give the starting year interval for the top movies. (Ex. 1995)");
                                String time = scanner.next();
                                System.out.println("Please give the ending year interval for the top movies. (Ex. 2000)");
                                String time2 = scanner.next();
                                movie.displayTopMovies(connection, time, time2);
                                break;
                            case 2:
                                System.out.println("Which movie review would you like to see?");
                                scanner.nextLine(); 
                                String movie_name = scanner.nextLine();
                                movie.displayMovieReviews(connection, movie_name);
                                break;
                            case 3:
                                break;
                            default:
                                System.out.println("Invalid choice. Please try again.");
                        }
                    }
                    break;
                case 10:
                    System.out.println("Exiting StarsRUs. Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

}