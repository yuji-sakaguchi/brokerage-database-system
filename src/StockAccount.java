import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StockAccount {
    public void checkStockAccountExist(Connection connection, String username, String stock_symbol) throws SQLException {
        boolean stockAccountExists = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM STOCK_ACCOUNTS WHERE c_username = ? AND stock_symbol = ?")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, stock_symbol);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                stockAccountExists = resultSet.next();
            } catch (SQLException e) {
                System.out.println("Error: you dont own any " + stock_symbol + " stocks");
                System.out.println(e);
            } 
        } catch (SQLException e) {
            System.out.println("Error stock_symbol doesnt exist.");
            System.out.println(e);
        } 

        if (!stockAccountExists) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO STOCK_ACCOUNTS (c_username, stock_shares, stock_symbol) VALUES (?, 0, ?)")) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, stock_symbol);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Stock_Account created successfully for user: " + username);
                } else {
                    System.out.println("Stock_Account insertion failed.");
                }
            }
        }
    } 

    public void buyStocks(Connection connection, String username, String stock_symbol, double stock_shares) throws SQLException {
        double balance = 0.0;
        double currprice = 0.0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT market_acc_balance FROM MARKET_ACCOUNTS WHERE c_username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    balance = resultSet.getDouble("market_acc_balance");
                }
            } 
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_price FROM Actors WHERE stock_symbol = ?")) {
            preparedStatement.setString(1, stock_symbol);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    currprice = resultSet.getDouble("curr_price");
                }
            } 
        }
        if(stock_shares * currprice + 20 > balance) {
            System.out.println("Error: Not enough money to buy " + stock_shares + " " + stock_symbol + "stocks.");
            return;
        }

        double old_stock_shares = 0.0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT stock_shares FROM STOCK_ACCOUNTS WHERE c_username = ? AND stock_symbol = ?")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, stock_symbol);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        old_stock_shares = resultSet.getDouble("stock_shares");
                    }
                }
            }
        balance = balance - (stock_shares * currprice) - 20;
        double stock_shares2 = stock_shares + old_stock_shares;
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE STOCK_ACCOUNTS SET stock_shares = ? WHERE c_username = ? AND stock_symbol = ?")) {
            preparedStatement.setDouble(1, stock_shares2);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, stock_symbol);
            
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\nNew stock shares: " + stock_shares2);
            } else {
                System.out.println("Stock account shares update did not work for user: " + username);
            }
        } catch (SQLException e) {
            System.out.println("Error updating user data.");
            System.out.println(e);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET market_acc_balance = ?, shares_traded = shares_traded + ? WHERE c_username = ?")) {
            preparedStatement.setDouble(1, balance);
            preparedStatement.setDouble(2, stock_shares);
            preparedStatement.setString(3, username);
            
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

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Stock_Shares_Accounts (c_username, stock_shares, stock_symbol, curr_price) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, stock_shares);
            preparedStatement.setString(3, stock_symbol);
            preparedStatement.setDouble(4, currprice);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Stock_Shares_Accounts insertion created successfully for user: ");
            } else {
                System.out.println("Stock_Shares_Accounts insertion failed.");
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
        double balance2 = stock_shares * currprice;
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Transactions (c_username, value, trans_type, trans_date, stock_symbol, stock_shares, curr_price) VALUES (?, ?, 'buy', TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, -balance2);
            preparedStatement.setString(3, day);
            preparedStatement.setString(4, stock_symbol);
            preparedStatement.setDouble(5, stock_shares);
            preparedStatement.setDouble(6, currprice);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Buy transaction insertion created successfully");
            } else {
                System.out.println("Buy transaction insertion failed.");
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

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Latest_Transaction (c_username, value, trans_type, stock_shares, stock_symbol, stock_shares_id) VALUES (?, ?, 'buy', ?, ?, 0)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, balance2);
            preparedStatement.setDouble(3, stock_shares);
            preparedStatement.setString(4, stock_symbol);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Latest_Transaction insertion created successfully");
            } else {
                System.out.println("Latest_Transaction insertion failed.");
            }
        }

        int id = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT MAX(stock_shares_id) FROM STOCK_SHARES_ACCOUNTS WHERE c_username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    id = resultSet.getInt(1);
                }
            }
        } 

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Latest_Transaction (c_username, value, trans_type, stock_shares, stock_symbol, stock_shares_id) VALUES (?, ?, 'buy', ?, ?, ?)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, balance2);
            preparedStatement.setDouble(3, stock_shares);
            preparedStatement.setString(4, stock_symbol);
            preparedStatement.setInt(5, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Latest_Transaction insertion created successfully");
            } else {
                System.out.println("Latest_Transaction insertion failed.");
            }
        }
    }

    public double sellStockShares(Connection connection, String username, String stock_symbol, double stock_shares, String id) throws SQLException {
        double balance = 0.0;
        double currprice = 0.0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT market_acc_balance FROM MARKET_ACCOUNTS WHERE c_username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    balance = resultSet.getDouble("market_acc_balance");
                }
            } 
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_price FROM Actors WHERE stock_symbol = ?")) {
            preparedStatement.setString(1, stock_symbol);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    currprice = resultSet.getDouble("curr_price");
                }
            } 
        }

        double old_stock_shares = 0.0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT stock_shares FROM STOCK_SHARES_ACCOUNTS WHERE stock_shares_id = ? AND c_username = ?")) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        old_stock_shares = resultSet.getDouble("stock_shares");
                    }
                }
            }

        balance = balance + (stock_shares * currprice);
        double stock_shares2 = old_stock_shares - stock_shares;
        if (stock_shares2 < 0) {
            System.out.println("Error: Cant sell more stocks shares than owned.");
            return 0.0;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Stock_Shares_Accounts SET stock_shares = ? WHERE stock_shares_id = ? AND c_username = ?")) {
            preparedStatement.setDouble(1, stock_shares2);
            preparedStatement.setString(2, id);
            preparedStatement.setString(3, username);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\nStock_Shares_Accounts updated successfully");
            } else {
                System.out.println("Stock_Shares_Accounts update failed.");
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE MARKET_ACCOUNTS SET market_acc_balance = ?, shares_traded = shares_traded + ? WHERE c_username = ?")) {
            preparedStatement.setDouble(1, balance);
            preparedStatement.setDouble(2, stock_shares);
            preparedStatement.setString(3, username);
            
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

        double balance2 = stock_shares * currprice;
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Latest_Transaction (c_username, value, trans_type, stock_shares, stock_symbol, stock_shares_id) VALUES (?, ?, 'sell', ?, ?, ?)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, balance2);
            preparedStatement.setDouble(3, stock_shares);
            preparedStatement.setString(4, stock_symbol);
            preparedStatement.setString(5, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Latest_Transaction insertion created successfully");
            } else {
                System.out.println("Latest_Transaction insertion failed.");
            }
        }

        return stock_shares;
    }

     public void sellStocks(Connection connection, String username, String stock_symbol, double total_stock_shares) throws SQLException {
        double old_stock_shares = 0.0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT stock_shares FROM STOCK_ACCOUNTS WHERE c_username = ? AND stock_symbol = ?")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, stock_symbol);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    old_stock_shares = resultSet.getDouble("stock_shares");
                }
            }
        }
        
        double new_stocks = old_stock_shares - total_stock_shares;
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE STOCK_ACCOUNTS SET stock_shares = ? WHERE c_username = ? AND stock_symbol = ?")) {
            preparedStatement.setDouble(1, new_stocks);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, stock_symbol);
            
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\nNew stock shares: " + new_stocks);
            } else {
                System.out.println("Stock account shares update did not work for user: " + username);
            }
        } catch (SQLException e) {
            System.out.println("Error updating user data.");
            System.out.println(e);
        }

        double currprice = 0.0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT curr_price FROM Actors WHERE stock_symbol = ?")) {
            preparedStatement.setString(1, stock_symbol);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    currprice = resultSet.getDouble("curr_price");
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
        
        double balance2 = total_stock_shares * currprice;
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Transactions (c_username, value, trans_type, trans_date, stock_symbol, stock_shares, curr_price) VALUES (?, ?, 'sell', TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, balance2);
            preparedStatement.setString(3, day);
            preparedStatement.setString(4, stock_symbol);
            preparedStatement.setDouble(5, total_stock_shares);
            preparedStatement.setDouble(6, currprice);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Sell transaction insertion created successfully");
            } else {
                System.out.println("Sell transaction insertion failed.");
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Latest_Transaction (c_username, value, trans_type, stock_shares, stock_symbol, stock_shares_id) VALUES (?, ?, 'sell', ?, ?, 0)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setDouble(2, balance2);
            preparedStatement.setDouble(3, total_stock_shares);
            preparedStatement.setString(4, stock_symbol);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Latest_Transaction insertion created successfully");
            } else {
                System.out.println("Latest_Transaction insertion failed.");
            }
        }
    }


    public void listSpecificStocks(Connection connection, String username, String stock_symbol) throws SQLException {
        System.out.println("\n--- Stocks ---");
        double stock_shares = 0.0;
        double price = 0.0;
        int stock_shares_id = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM STOCK_SHARES_ACCOUNTS WHERE c_username = ? AND stock_symbol = ?")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, stock_symbol);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    stock_shares_id = resultSet.getInt("stock_shares_id");
                    stock_shares = resultSet.getDouble("stock_shares");
                    stock_symbol = resultSet.getString("stock_symbol");
                    price = resultSet.getDouble("curr_price");
                    System.out.println("ID(" + stock_shares_id + "): " + stock_shares + " " + stock_symbol + " stocks that were bought for $" + price);
                } 
            }
        }
    }

}