package distsite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import utils.customPrint;

public class DataProcessor {

    private String DB_URL;

    public DataProcessor(String DB_Name) {
        this.DB_URL = "jdbc:sqlite:" + DB_Name + ".db";
    }

    public void DBInit() {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection(DB_URL);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists data");
            statement.executeUpdate("create table data (name char, value integer)");
            for (int i = 0; i < 5; i++) {
                statement.executeUpdate(String.format("insert into data values('%c', 0)",
                        (char) ('A' + i)));
            }
            // ResultSet rs = statement.executeQuery("select * from data");
            // while (rs.next()) {
            // // read the result set
            // customPrint.printout(String.format("name = %s, value = %d",
            // rs.getString("name"), rs.getInt("value")));
            // }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            customPrint.printerr(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                customPrint.printerr(e.getMessage());
            }
        }
    }

    private String sqlRead(String item) {
        return String.format("select value from data where name = '%s'", item);
    }

    private String sqlWriteUpdate(String item, Integer value) {
        // customPrint.printout(String.format("update data set value = %d where name =
        // '%s'", value, item));
        return String.format("update data set value = %d where name = '%s'", value, item);
    }

    private String sqlWriteInsert(String item, Integer value) {
        // INSERT INTO table_name (column1, column2, column3, ...) VALUES (value1,
        // value2, value3, ...);
        // customPrint.printout(String.format("insert into data (name, value) values
        // ('%s', %d)", item, value));
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
            customPrint.printerr(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                customPrint.printerr(e.getMessage());
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
            // customPrint.printout("Begin to write into database...");

            for (Map.Entry<String, Integer> values : valueMap.entrySet()) {
                String item = values.getKey();
                Integer value = values.getValue();
                // customPrint.printout("Item: " + item + " Value: " + value);
                ResultSet rs = statement.executeQuery(sqlRead(item));
                if (!rs.next()) {
                    statement.executeUpdate(sqlWriteInsert(item, value));
                } else {
                    statement.executeUpdate(sqlWriteUpdate(item, value));
                }
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            customPrint.printerr(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                customPrint.printerr(e.getMessage());
            }
        }
    }

    public void display() {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection(DB_URL);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            ResultSet rs = statement.executeQuery("select * from data");
            System.out.println("Current Database: ");
            while (rs.next()) {
                // read the result set
                System.out.println(String.format("[%s: %d]",
                        rs.getString("name"), rs.getInt("value")));
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            customPrint.printerr(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                customPrint.printerr(e.getMessage());
            }
        }
    }

}
