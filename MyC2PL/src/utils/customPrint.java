package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class customPrint {

    public static String timestamp() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        /* Write the timestamp */
        return (dtf.format(now) + ": ");
    }

    public static void printout(String str) {
        System.out.println(timestamp() + str);
    }

    public static void printerr(String str) {
        System.err.println(timestamp() + str);
    }
}
