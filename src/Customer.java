import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Customer {
    public void login(Connection connection, String username, String password) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM CUSTOMERS WHERE c_username = ? AND c_password = ?")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("Login successful for user: " + username);
                    String name = resultSet.getString("c_username");
                    if (name.equals("admin")) {
                        UserInterface userInterface = new UserInterface();
                        userInterface.managerInterace(username, connection);
                    }
                    else {
                    UserInterface userInterface = new UserInterface();
                    userInterface.traderInterace(username, connection);
                    }
                } else {
                    System.out.println("Invalid username or password.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error checking login user credentials.");
            System.out.println(e);
        }
    }

    public void customerReport(Connection connection, String username) throws SQLException  {
        double balance = 0.0;
        int market_id = 0;
        System.out.println("\n--- " + username + "'s Customer Report ---");
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM MARKET_ACCOUNTS WHERE c_username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    balance = resultSet.getDouble("market_acc_balance");
                    market_id = resultSet.getInt("market_acc_id");
                    System.out.println("ID(" + market_id + "): Market Account Balance: $" + balance);
                } else {
                    System.out.println("User not found: " + username);
                }
            }
        }
        double stock_shares = 0.0;
        String stock_symbol = "";
        double price = 0.0;
        int stock_shares_id = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM STOCK_SHARES_ACCOUNTS WHERE c_username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    stock_shares_id = resultSet.getInt("stock_shares_id");
                    stock_shares = resultSet.getDouble("stock_shares");
                    stock_symbol = resultSet.getString("stock_symbol");
                    price = resultSet.getDouble("curr_price");
                    System.out.println("ID(" + stock_shares_id + "): " + stock_shares + " " + stock_symbol + " stocks in Stock Account that were bought for $" + price);
                } 
            }
        }
    }

}