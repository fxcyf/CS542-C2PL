package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransactionGen {
    // public static String randomSelect() {
    // Random rand = new Random();
    // int randNum = rand.nextInt(5);
    // char randName = (char) ('A' + randNum);
    // return String.format("select value from data where name = %c;", randName);
    // }

    // public static String randomUpdate() {
    // Random rand = new Random();
    // int randNum = rand.nextInt(5);
    // char randName = (char) ('A' + randNum);
    // int randValue = rand.nextInt(20) + 1;
    // return String.format("update data set value = value + %d where name = %c;",
    // randValue, randName);
    // }

    /**
     * The main function for generating random transactions
     * Expected arguments:
     * [0] number of transactions to generate
     * [1] number of statements in each transaction (select / update)
     * [2] output path
     * 
     * @param args
     */
    public static void main(String[] args) {
        int numTransaction = Integer.parseInt(args[0]);
        int lenTransaction = Integer.parseInt(args[1]);
        String out_file = args[2];
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = Paths.get(currentPath.toString(),
                "transactions", out_file);

        Random rand = new Random();

        // List<List<String>> transactions = new ArrayList<>();
        Character[] items = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
        try {
            BufferedWriter f_writer = new BufferedWriter(new FileWriter(filePath.toString()));

            for (int i = 0; i < numTransaction; i++) {
                f_writer.write("TRANSACTION: \n");
                // List<String> transaction = new ArrayList<>();
                for (int j = 0; j < lenTransaction; j++) {
                    // Randomly generate select and update statements
                    int randNum = rand.nextInt(items.length);
                    f_writer.write(String.format("r(%c);\n", items[randNum]));
                    f_writer.write(String.format("m%c=%c+%d;\n", items[randNum], items[randNum], rand.nextInt(20)));
                    f_writer.write(String.format("w(%c);\n", items[randNum]));
                    // if (randNum < 0.2) {
                    // f_writer.write(randomSelect() + "\n");
                    // } else {
                    // f_writer.write(randomUpdate() + "\n");
                    // }
                }
            }
            f_writer.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
}
