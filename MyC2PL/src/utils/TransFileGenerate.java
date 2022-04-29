package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class TransFileGenerate {

    public static String randomSelect() {
        Random rand = new Random();
        int randNum = rand.nextInt(5);
        char randName = (char) ('A' + randNum);
        return String.format("select value from data where name = %c;", randName);
    }

    public static String randomUpdate() {
        Random rand = new Random();
        int randNum = rand.nextInt(5);
        char randName = (char) ('A' + randNum);
        int randValue = rand.nextInt(20) + 1;
        return String.format("update data set value = value + %d where name = %c;", randValue, randName);
    }

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
        int size = Integer.parseInt(args[0]);
        int len = Integer.parseInt(args[1]);
        String out_file = args[2];
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = Paths.get(currentPath.toString(),
                "transactions", out_file);
        Path dirPath = Paths.get(currentPath.toString(), "transactions");

        File directory = new File(dirPath.toString());

        if (!directory.exists()) {
            directory.mkdir();
        }
        Random rand = new Random();

        // List<List<String>> transactions = new ArrayList<>();

        try {
            BufferedWriter f_writer = new BufferedWriter(new FileWriter(filePath.toString()));

            for (int i = 0; i < size; i++) {
                f_writer.write("Transaction: \n");
                // List<String> transaction = new ArrayList<>();
                for (int j = 0; j < len; j++) {
                    // Randomly generate select and update statements
                    Double randNum = rand.nextDouble();

                    if (randNum < 0.2) {
                        f_writer.write(randomSelect() + "\n");
                    } else {
                        f_writer.write(randomUpdate() + "\n");
                    }
                }
            }
            f_writer.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
}
