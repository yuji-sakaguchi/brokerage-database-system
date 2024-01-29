import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Movie {
    public void displayMovies(Connection connection) throws SQLException {
        System.out.println("\n--- Movies ---");
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Movies")) {  
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String title = resultSet.getString("movie_title");
                    String year = resultSet.getString("movie_year");
                    double rating = resultSet.getDouble("movie_rating");
                    System.out.println(title + ", Year: " + year + ", Rating: " + rating);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving data from the column.");
            System.out.println(e);
        }
    }

    public void displayTopMovies(Connection connection, String time, String time2) throws SQLException {
        System.out.println("\n--- Movies ---");
        boolean found = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Movies WHERE movie_rating = (SELECT MAX(movie_rating) FROM Movies WHERE movie_year BETWEEN ? AND ?)")) {  
            preparedStatement.setString(1, time);
            preparedStatement.setString(2, time2);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    found = true;
                    String title = resultSet.getString("movie_title");
                    String year = resultSet.getString("movie_year");
                    double rating = resultSet.getDouble("movie_rating");
                    System.out.println(title + ", Year: " + year + ", Rating: " + rating);
                }
                if (!found) {
                    System.out.println("No movies between those years in database.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving data from the column.");
            System.out.println(e);
        }
    }

    public void displayMovieReviews(Connection connection, String movie_name) throws SQLException {
        System.out.println("\n--- " + movie_name + " Reviews ---");
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Movie_Reviews WHERE movie_title = ?")) { 
            preparedStatement.setString(1, movie_name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.isBeforeFirst()) {
                    System.out.println("Please enter a valid movie title from the list (Did you spell it correctly?)");
                }
                else {
                    while (resultSet.next()) {
                        String review = resultSet.getString("movie_review");
                        System.out.println(review);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving data from the column.");
            System.out.println(e);
        }
    }
    
}