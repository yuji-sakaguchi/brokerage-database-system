import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Actor {
    public void listStocks(Connection connection) {
        System.out.println("\n--- Stocks ---");
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACTORS")) {  
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String stockname = resultSet.getString("stock_symbol");
                    String name = resultSet.getString("a_name");
                    double currprice = resultSet.getDouble("curr_price");
                    String birthdate = resultSet.getString("a_birthdate");

                    try (PreparedStatement movieStatement = connection.prepareStatement("SELECT * FROM MOVIE_CONTRACTS WHERE a_name = ?")) {  
                        movieStatement.setString(1, name);
                        try (ResultSet movieResultSet = movieStatement.executeQuery()) {
                            while (movieResultSet.next()) {
                                String movietitle = movieResultSet.getString("movie_title");
                                String movierole = movieResultSet.getString("movie_role");
                                String movieyear = movieResultSet.getString("movie_year");
                                double contractvalue = movieResultSet.getDouble("contract_value");
                                System.out.println(stockname + ", $" + currprice + ", " + name + ", " + birthdate + ", " + movietitle + ", " + movierole + ", " + movieyear + ", Contract value $" + contractvalue);
                            }              
                        }
                    } catch (SQLException e) {
                        System.out.println("Error retrieving data from the column.");
                        System.out.println(e);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving data from the column.");
            System.out.println(e);
        }
    }

}