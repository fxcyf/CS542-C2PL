package distsite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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

    private String sqlRead(String item) {
        return String.format("select value from data where name = %s", item);
    }

    private String sqlWriteUpdate(String item, Integer value) {
        return String.format("update data set value = %d where name = '%s'", value, item);
    }

    private String sqlWriteInsert(String item, Integer value) {
        // INSERT INTO table_name (column1, column2, column3, ...) VALUES (value1,
        // value2, value3, ...);
        return String.format("insert into data (name, value) values ('%s', %d)", item, value);
    }

    public int read(String item) {
        Connection connection = null;
        int res = 0;
        try {
            // create a database connection
            connection = DriverManager.getConnection(DB_URL);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            ResultSet rs = statement.executeQuery(sqlRead(item));
            while (rs.next()) {
                res = rs.getInt("value");
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
        return res;
    }

    public void write(Map<String, Integer> valueMap) {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection(DB_URL);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            for (Map.Entry<String, Integer> values : valueMap.entrySet()) {
                String item = values.getKey();
                Integer value = values.getValue();
                ResultSet rs = statement.executeQuery(sqlRead(item));
                if (rs.wasNull()) {
                    statement.executeQuery(sqlWriteInsert(item, value));
                } else {
                    statement.executeQuery(sqlWriteUpdate(item, value));
                }
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
