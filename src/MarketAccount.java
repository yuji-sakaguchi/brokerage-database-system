import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class MarketAccount {
    public void checkBal(Connection connection, String username, double balance) throws SQLException  {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT market_acc_balance FROM MARKET_ACCOUNTS WHERE c_username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    balance = resultSet.getDouble("market_acc_balance");
                    System.out.println("\nBalance for user " + username + ": $" + balance);
                } else {
                    System.out.println("User not found: " + username);
                }
            } catch (SQLException e) {
                System.out.println("Error retrieving balance information.");
                System.out.println(e);
            }
        }
    }

    public double checkBal2(Connection connection, String username) throws SQLException  {
        double money = 0.0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT market_acc_balance FROM MARKET_ACCOUNTS WHERE c_username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    money = resultSet.getDouble("market_acc_balance");
                }
            } 
        }
        return money;
    }

    public void deposit(Connection connection, String username, double balance) throws SQLException {
        double balance2 = checkBal2(connection, username);
        balance2 = balance2 + balance;
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET market_acc_balance = ? WHERE c_username = ?")) {
            preparedStatement.setDouble(1, balance2);
            preparedStatement.setString(2, username);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\nUpdate successful for user: " + username);
                System.out.println("New balance: $" + balance2);
            } else {
                System.out.println("User not found or update did not work for user: " + username);
            }
        } catch (SQLException e) {
            System.out.println("Error updating user data.");
            System.out.println(e);
        }

        String day = "";
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    day = resultSet.getString("curr_day");
                    day = day.substring(0, 10);
                }
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Transactions (c_username, value, trans_type, trans_date, stock_symbol, stock_shares, curr_price) VALUES (?, ?, 'deposit', TO_DATE(?, 'YYYY-MM-DD'), NULL, NULL, NULL)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, balance);
            preparedStatement.setString(3, day);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deposit transaction insertion created successfully for user: ");
            } else {
                System.out.println("Deposit transaction insertion failed.");
            }
        }
    }

    public void withdraw(Connection connection, String username, double balance) throws SQLException {
        double balance2 = checkBal2(connection, username);
        balance2 = balance2 - balance;
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET market_acc_balance = ? WHERE c_username = ?")) {
            preparedStatement.setDouble(1, balance2);
            preparedStatement.setString(2, username);
        
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\nUpdate successful for user: " + username);
                System.out.println("New balance: $" + balance2);
            } else {
                System.out.println("User not found or update did not work for user: " + username);
            }
        } catch (SQLException e) {
            System.out.println("Error updating user data.");
            System.out.println(e);
        }
        
        String day = "";
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    day = resultSet.getString("curr_day");
                    day = day.substring(0, 10);
                }
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Transactions (c_username, value, trans_type, trans_date, stock_symbol, stock_shares, curr_price) VALUES (?, ?, 'withdraw', TO_DATE(?, 'YYYY-MM-DD'), NULL, NULL, NULL)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, -balance);
            preparedStatement.setString(3, day);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Withdraw transaction insertion created successfully.");
            } else {
                System.out.println("Withdraw transaction insertion failed.");
            }
        }
    }

    public void addInterest(Connection connection, LocalDateTime date) throws SQLException {
        String guy = "";
        double balance = 0.0;
        double interest = 0.0;
        try (PreparedStatement personStatement = connection.prepareStatement("SELECT c_username FROM MARKET_ACCOUNTS")) {
            try (ResultSet personResultSet = personStatement.executeQuery()) {
                while (personResultSet.next()) {
                    guy = personResultSet.getString("c_username");
                    try (PreparedStatement readyStatement = connection.prepareStatement("SELECT * FROM Daily_Bal WHERE c_username = ?")) {
                        readyStatement.setString(1, guy);
                        try (ResultSet readyResultSet = readyStatement.executeQuery()) {
                            while (readyResultSet.next()) {
                                balance = balance + readyResultSet.getDouble("bal");
                            }
                        }
                    }
                    int daysInMonth = date.getMonth().maxLength();
                    balance = balance / daysInMonth;
                    interest = (balance * 1.10) - balance;
                    try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Market_Accounts SET market_acc_balance = ? * 1.10, interest_prev = ? WHERE c_username = ?")) {
                        preparedStatement.setDouble(1, balance);
                        preparedStatement.setDouble(2, interest);
                        preparedStatement.setString(3, guy);
                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                        } else {
                            System.out.println("Interest was not added.");
                        }
                    }
                    balance = 0.0;
                }
            }
        }   
    }

}