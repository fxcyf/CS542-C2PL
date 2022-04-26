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

    String file_name;

    int siteId;
    int transaction_count;
    int transaction_id_offset = 10000;
    int current_transaction_index;
    List<Transaction> history;

    public TransactionManager(int siteId, String file_name) {
        this.file_name = file_name;
        this.siteId = siteId;
        current_transaction_index = -1;
        transaction_count = 0;

        history = new ArrayList<>();
    }

    public int getNewTID() {
        transaction_count++;
        return siteId * transaction_id_offset + transaction_count;
    }

    public List<Operation> transfer_update(int tid, String update_st) {
        List<Operation> ops = new ArrayList<>();
        String pattern = "update data set value = value (\\+|\\-|\\*|\\/) (\\d+) where name = ([A-Z]);";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(update_st.trim());
        if (m.find()) {
            ops.add(new Operation(tid, "read", m.group(2)));
            ops.add(new Operation(tid, "compute", m.group(2), m.group(2), m.group(1), m.group(1).charAt(0)));
            ops.add(new Operation(tid, "write", m.group(2)));
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
            ops.add(new Operation(tid, "read", m.group(1)));
        } else {
            System.out.println("Invalid select statement " + select_st);
        }
        return ops;
    }

    public void load_history() throws Exception {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = Paths.get(currentPath.toString(),
                "transactions", file_name);

        // Creating an object of BufferedReader class
        BufferedReader br = new BufferedReader(new FileReader(filePath.toString()));

        // Declaring a string variable
        String sql_st;
        Transaction transaction = null;
        while ((sql_st = br.readLine()) != null) {
            if (sql_st.toLowerCase().contains("transaction")) {
                if (transaction != null) {
                    transaction.addOperation(new Operation(transaction.getTid(), "commit"));
                    history.add(transaction);
                }
                transaction = new Transaction(getNewTID());
            } else if (sql_st.toLowerCase().contains("update")) {
                List<Operation> updateOps = transfer_update(transaction.getTid(), sql_st);
                if (updateOps.size() > 0) {
                    transaction.addOperations(updateOps);
                }
            } else if (sql_st.toLowerCase().contains("select")) {
                List<Operation> selectOp = transfer_select(transaction.getTid(), sql_st);
                if (selectOp.size() > 0) {
                    transaction.addOperations(selectOp);
                }
            }
        }
        if (transaction != null) {
            transaction.addOperation(new Operation(transaction.getTid(), "commit"));
            history.add(transaction);
        }
    }

    public Transaction getNextTransaction() {
        current_transaction_index = (current_transaction_index + 1) % transaction_count;
        return history.get(current_transaction_index);
    }

}
