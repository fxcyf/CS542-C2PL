package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInit {

    /**
     * The main function for initializing database
     * Expected arguments: db_name
     * 
     * @param args
     */
    public static void main(String[] args) {
        Connection connection = null;
        try {
            String db_name = args[0];
            // create a database connection
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s.db", db_name));
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists data");
            statement.executeUpdate("create table data (name char, value integer)");
            for (int i = 0; i < 5; i++) {

                // statement.executeUpdate(
                // "insert into data values('" + (char) ('A' + i) + "', " + String.valueOf(i +
                // 1) + ")");
                statement.executeUpdate(String.format("insert into data values('%c', %d)",
                        (char) ('A' + i), i + 1));
            }
            ResultSet rs = statement.executeQuery("select * from data");
            while (rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("value = " + rs.getInt("value"));
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }
}