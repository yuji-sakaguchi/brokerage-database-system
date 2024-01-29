import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    public void getTransactionList(Connection connection, String username) throws SQLException {
        System.out.println("\n--- Transactions ---");
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTIONS WHERE c_username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String trans_type = resultSet.getString("trans_type");
                    if (trans_type.equals("buy") || trans_type.equals("sell")) {
                        double value = resultSet.getDouble("value");
                        String trans_date = resultSet.getString("trans_date");
                        trans_date = trans_date.substring(0, 10);
                        String stock_symbol = resultSet.getString("stock_symbol");
                        double stock_shares = resultSet.getDouble("stock_shares");
                        double currprice = resultSet.getDouble("curr_price");
                        System.out.println(trans_date + ", " + trans_type + ", Total: $" + value + ", " + stock_shares + " stocks of " + stock_symbol + " for $" + currprice + " each");
                    }
                    else {
                        double value = resultSet.getDouble("value");
                        String trans_date = resultSet.getString("trans_date");
                        trans_date = trans_date.substring(0, 10);
                        System.out.println(trans_date + ", " + trans_type + ", $" + value);
                    }
                }
            } 
        }
    }

    public void generateMonthlyStatement(Connection connection, String name) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Customers WHERE c_username = ?")) {
            preparedStatement.setString(1, name);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String fullname = resultSet.getString("c_name");
                    String email = resultSet.getString("c_email");
                    System.out.println("\nMonthly statement for " + fullname + ", " + email);
                }
            }
        }

        int commissions = 0;
        double total = 0.0;
        System.out.println("\n--- Transactions ---");
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTIONS WHERE c_username = ?")) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String trans_type = resultSet.getString("trans_type");
                    if (trans_type.equals("buy") || trans_type.equals("sell")) {
                        double value = resultSet.getDouble("value");
                        total = total + value;
                        String trans_date = resultSet.getString("trans_date");
                        trans_date = trans_date.substring(0, 10);
                        String stock_symbol = resultSet.getString("stock_symbol");
                        double stock_shares = resultSet.getDouble("stock_shares");
                        double currprice = resultSet.getDouble("curr_price");
                        commissions = commissions + 20;
                        System.out.println(trans_date + ", " + trans_type + ", Total: $" + value + ", " + stock_shares + " stocks of " + stock_symbol + " for $" + currprice + " each");
                    }
                    else {
                        double value = resultSet.getDouble("value");
                        String trans_date = resultSet.getString("trans_date");
                        trans_date = trans_date.substring(0, 10);
                        if (trans_type.equals("cancel")) {
                            commissions = commissions + 20;
                            total = total + value;
                        }
                        System.out.println(trans_date + ", " + trans_type + ", $" + value);
                    }
                }
            } 
        }

        double initial_bal = 0.0;
        double final_bal = 0.0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM MARKET_ACCOUNTS WHERE c_username = ?")) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    initial_bal = resultSet.getDouble("initial_bal");
                    final_bal = resultSet.getDouble("market_acc_balance");
                }
            }
        }

        double balance = 0.0;
        double interest = 0.0;
        String today = "";
        LocalDateTime date = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_day FROM DAY WHERE DAY_ID = 1")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    today = resultSet.getString("curr_day");
                    date = LocalDateTime.parse(today, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            }
        }

        try (PreparedStatement readyStatement = connection.prepareStatement("SELECT * FROM Daily_Bal WHERE c_username = ?")) {
            readyStatement.setString(1, name);
            try (ResultSet readyResultSet = readyStatement.executeQuery()) {
                while (readyResultSet.next()) {
                    balance = balance + readyResultSet.getDouble("bal");
                }
            }
        }

        int dayInMonth = date.getDayOfMonth() - 1;
        if (dayInMonth != 0) {
            balance = balance / dayInMonth;
        }
        interest = (balance * 1.10) - balance;

        total = total - commissions + interest;
        System.out.println("\nTotal amount of commissions paid: $" + commissions);
        System.out.println("Inital Balance $" + initial_bal);
        System.out.println("Final Balance: $" + final_bal);
        System.out.println("Total Earnings (including interest so far this month and commission fees): $" + String.format("%.2f", total));
    }

    public void listActiveCustomers(Connection connection) throws SQLException {
        System.out.println("\n--- Active Customers ---");
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM MARKET_ACCOUNTS WHERE shares_traded >= 1000")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("c_username");
                    System.out.println(name);
                }
            }
        }
    }

    public void listDTER(Connection connection) throws SQLException {
        System.out.println("\n--- Government Drug & Tax Evasion Report ---");
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM MARKET_ACCOUNTS WHERE DTER = 1")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("c_username");
                    try (PreparedStatement customerStatement = connection.prepareStatement("SELECT c_state FROM Customers WHERE c_username = ?")) {
                        customerStatement.setString(1, name);
                        try (ResultSet customerResultSet = customerStatement.executeQuery()) {
                            if (customerResultSet.next()) {
                                String state = customerResultSet.getString("c_state");
                                System.out.println(name + ", " + state);
                            }
                        }
                    }
                }
            }
        }
    }

    public void cancelLatestTransaction(Connection connection, String username) throws SQLException {
        double cancelMoney = 0.0;
        System.out.println("");
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM LATEST_TRANSACTION WHERE c_username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    double value = resultSet.getDouble("value");
                    String trans_type = resultSet.getString("trans_type");
                    double stock_shares = resultSet.getDouble("stock_shares");
                    String stock_symbol = resultSet.getString("stock_symbol");
                    int stock_shares_id = resultSet.getInt("stock_shares_id");
                    if (stock_shares_id == 0) {
                        if (trans_type.equals("buy")) {
                            cancelMoney = value;
                            try (PreparedStatement stockStatement = connection.prepareStatement("UPDATE STOCK_ACCOUNTS SET stock_shares = stock_shares - ? WHERE c_username = ? AND stock_symbol = ?")) {
                                stockStatement.setDouble(1, stock_shares);
                                stockStatement.setString(2, username);
                                stockStatement.setString(3, stock_symbol);
                                
                                int rowsAffected = stockStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    System.out.println("Reversed stock shares");
                                } else {
                                    System.out.println("Reverse stock account shares did not work for user: " + username);
                                }
                            } catch (SQLException e) {
                                System.out.println("Error updating user data.");
                                System.out.println(e);
                            }

                            try (PreparedStatement stockStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET market_acc_balance = market_acc_balance + ? - 20, shares_traded = shares_traded - ? WHERE c_username = ?")) {
                                stockStatement.setDouble(1, value);
                                stockStatement.setDouble(2, stock_shares);
                                stockStatement.setString(3, username);
                                
                                int rowsAffected = stockStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    System.out.println("Reversed market account balance");
                                } else {
                                    System.out.println("Reverse market account balance did not work for user: " + username);
                                }
                            } catch (SQLException e) {
                                System.out.println("Error updating user data.");
                                System.out.println(e);
                            }
                        }
                        else {
                            cancelMoney = -value;
                            try (PreparedStatement stockStatement = connection.prepareStatement("UPDATE STOCK_ACCOUNTS SET stock_shares = stock_shares + ? WHERE c_username = ? AND stock_symbol = ?")) {
                                stockStatement.setDouble(1, stock_shares);
                                stockStatement.setString(2, username);
                                stockStatement.setString(3, stock_symbol);
                                
                                int rowsAffected = stockStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    System.out.println("Reversed stock shares");
                                } else {
                                    System.out.println("Reverse stock account shares did not work for user: " + username);
                                }
                            } catch (SQLException e) {
                                System.out.println("Error updating user data.");
                                System.out.println(e);
                            }

                            try (PreparedStatement stockStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET market_acc_balance = market_acc_balance - ? - 20, shares_traded = shares_traded - ? WHERE c_username = ?")) {
                                stockStatement.setDouble(1, value);
                                stockStatement.setDouble(2, stock_shares);
                                stockStatement.setString(3, username);
                                
                                int rowsAffected = stockStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    System.out.println("Reversed market account balance");
                                } else {
                                    System.out.println("Reverse market account balance did not work for user: " + username);
                                }
                            } catch (SQLException e) {
                                System.out.println("Error updating user data.");
                                System.out.println(e);
                            }
                        }
                    }
                    else {
                        if (trans_type.equals("buy")) {
                            try (PreparedStatement stockStatement = connection.prepareStatement("UPDATE STOCK_SHARES_ACCOUNTS SET stock_shares = stock_shares - ? WHERE stock_shares_id = ?")) {
                                stockStatement.setDouble(1, stock_shares);
                                stockStatement.setInt(2, stock_shares_id);

                                int rowsAffected = stockStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    System.out.println("Reversed stock shares account");
                                } else {
                                    System.out.println("Reversed stock shares account did not work for user: " + username);
                                }
                            } catch (SQLException e) {
                                System.out.println("Error updating user data.");
                                System.out.println(e);
                            }
                        } 
                        else {
                            try (PreparedStatement stockStatement = connection.prepareStatement("UPDATE STOCK_SHARES_ACCOUNTS SET stock_shares = stock_shares + ? WHERE stock_shares_id = ?")) {
                                stockStatement.setDouble(1, stock_shares);
                                stockStatement.setInt(2, stock_shares_id);

                                int rowsAffected = stockStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    System.out.println("Reversed stock shares account");
                                } else {
                                    System.out.println("Reversed stock shares account did not work for user: " + username);
                                }
                            } catch (SQLException e) {
                                System.out.println("Error updating user data.");
                                System.out.println(e);
                            }
                        }
                    }
                } 
            }
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

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Transactions (c_username, value, trans_type, trans_date, stock_symbol, stock_shares, curr_price) VALUES (?, ?, 'cancel', TO_DATE(?, 'YYYY-MM-DD'), NULL, NULL, NULL)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, cancelMoney);
            preparedStatement.setString(3, day);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Cancel transaction insertion created successfully.");
            } else {
                System.out.println("Cancel transaction insertion failed.");
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Latest_Transaction WHERE c_username = ?")) {
            preparedStatement.setString(1, username);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Latest_Transaction rows deleted successfully ");
            } else {
                System.out.println("Latest_Transaction rows were not deleted.");
            }
        }
    }

}