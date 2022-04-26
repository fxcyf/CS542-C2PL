package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInit {
    public static final String DB_JDBC = "org.sqlite.JDBC";

    /* URL to the data base in SQLite using JDBC */
    public static String DB_URL = "jdbc:sqlite:c2pl.db";

    public static void main(String[] args) {

        /* The connection object to the database */
        Connection connection = null;

        /* The result code after executing the query in the database */

        /* Try to connect to the database and execute the query */
        try {

            /* Connect to the database using JDBC */
            Class.forName(DB_JDBC);
            connection = DriverManager.getConnection(DB_URL);

            /* Generate the statement */
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            /* Drop any previous table */
            statement.executeUpdate("drop table if exists items");

            /* Create the table */
            String query = "create table items (item string, value integer)";
            statement.executeUpdate(query);
            char[] items = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
            for (int i = 0; i < items.length; i++) {
                statement.executeUpdate(String.format("insert into items values('%c', %d)",
                        items[i], 0));
            }
            /* Close the statement and the connection to the database */
            statement.close();
            connection.close();

        } catch (ClassNotFoundException e) {
            System.err.println(" Class Not Found Exception: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println(" SQL Exception: " + e.getMessage());
        }

        /* Return the result from the database */
    }
}
