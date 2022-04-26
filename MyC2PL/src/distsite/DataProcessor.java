package distsite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.management.openmbean.OpenDataException;

import elements.Operation;

public class DataProcessor {

    private String DB_URL;

    public DataProcessor(String DB_URL) {
        this.DB_URL = DB_URL;
    }

    public void initializeData() {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection(DB_URL);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists data");
            statement.executeUpdate("create table data (name char, value integer)");
            for (int i = 0; i < 5; i++) {
                statement.executeUpdate(String.format("insert into data values('%c', %d)",
                        (char) ('A' + i), i + 1));
            }
            ResultSet rs = statement.executeQuery("select * from data");
            while (rs.next()) {
                // read the result set
                System.out.println(String.format("name = %s, value = %d",
                        rs.getString("name"), rs.getInt("value")));
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

    public int execute(Operation op) {
        switch (op.getType())
    }

}
