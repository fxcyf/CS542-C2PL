package distsite;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import elements.Operation;
import elements.Transaction;

public class TransactionManager {

    private String transaction_file;

    private int siteID;
    private int transactionNum;
    private int TIDOffset = 10000;
    private int curTransIndex;
    private List<Transaction> transactions;

    public TransactionManager(int siteID, String file_name) {

        this.transaction_file = file_name;
        this.siteID = siteID;
        transactionNum = 0;
        curTransIndex = -1;

        transactions = new ArrayList<>();
    }

    public int getNewTID() {
        transactionNum++;
        return siteID * TIDOffset + transactionNum;
    }

    public List<Operation> transfer_update(int tid, String update_st) {
        List<Operation> ops = new ArrayList<>();
        // update data set value = value [+] [1] where name = [a]
        String pattern = "update data set value = value (\\+|\\-|\\*|\\/) (\\d+) where name = ([A-Z]);";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(update_st.trim());
        if (m.find()) {
            String arg = m.group(2);
            String num = m.group(1);
            char operator = m.group(0).charAt(0);

            // read(a)
            ops.add(new Operation(tid, Operation.readType, arg));
            // a = a + 1
            ops.add(new Operation(tid, Operation.mathType, arg, arg, num, operator));
            // write(a)
            ops.add(new Operation(tid, Operation.writeType, m.group(2)));
        } else {
            System.out.println("Invalid update statement: " + update_st);
        }

        return ops;
    }

    public List<Operation> transfer_select(int tid, String select_st) {
        List<Operation> ops = new ArrayList<>();
        String pattern = "select value from data where name = ([A-Z]);";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(select_st);
        if (m.find()) {
            ops.add(new Operation(tid, Operation.readType, m.group(1)));
        } else {
            System.out.println("Invalid select statement " + select_st);
        }
        return ops;
    }

    public void load_history() throws Exception {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = Paths.get(currentPath.toString(),
                "transactions", transaction_file);

        // Creating an object of BufferedReader class
        BufferedReader br = new BufferedReader(new FileReader(filePath.toString()));

        // Declaring a string variable
        String sql_st;
        Transaction transaction = null;
        while ((sql_st = br.readLine()) != null) {
            if (sql_st.toLowerCase().contains("transaction")) {
                if (transaction != null) {
                    transaction.addOperation(new Operation(transaction.getTID(), Operation.commitType));
                    transactions.add(transaction);
                }
                transaction = new Transaction(getNewTID());
            } else if (sql_st.toLowerCase().contains("update")) {
                List<Operation> updateOps = transfer_update(transaction.getTID(), sql_st);
                if (updateOps.size() > 0) {
                    transaction.addOperations(updateOps);
                }
            } else if (sql_st.toLowerCase().contains("select")) {
                List<Operation> selectOp = transfer_select(transaction.getTID(), sql_st);
                if (selectOp.size() > 0) {
                    transaction.addOperations(selectOp);
                }
            }
        }
        if (transaction != null) {
            transaction.addOperation(new Operation(transaction.getTID(), Operation.commitType));
            transactions.add(transaction);
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Transaction getNextTransaction() {
        curTransIndex++;
        return transactions.get(curTransIndex);
    }

    public int getTransNum() {
        return transactionNum;
    }

}
