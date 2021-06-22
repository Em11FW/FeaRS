package arule;

import au.com.bytecode.opencsv.CSVWriter;
import analyser_db.PsqlDB;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TransactionsCSVWriter {
    private static TransactionsCSVWriter transactionsCSVWriterInstance = null;
    private HashMap<String, Set<String> > transactionsMap = new HashMap<>();

    private TransactionsCSVWriter(){ }

    public static TransactionsCSVWriter getInstance(){
        if(transactionsCSVWriterInstance ==null)
            transactionsCSVWriterInstance = new TransactionsCSVWriter();
        return transactionsCSVWriterInstance;
    }

    private void populateTransactionsMap() throws SQLException {
        ResultSet methods = PsqlDB.getInstance().getMethodsWithClusterInfo();
        while (methods.next()) {
            long commitId = methods.getLong("commit_id");
            long clusterId = methods.getLong("cluster_id");
            String file_path = methods.getString("filepath");

            String commitId_filePath__key = String.format("%d_%s", commitId, file_path); // transaction: new methods at a commit at the same file

            if (transactionsMap.containsKey(commitId_filePath__key)) {
                transactionsMap.get(commitId_filePath__key).add(Long.toString(clusterId));
            } else {
                Set<String> newSet = new HashSet<>();
                newSet.add(Long.toString(clusterId));
                transactionsMap.put(commitId_filePath__key, newSet);
            }
        }
    }


    public boolean writeTransactionsCSV(String filePath){
        try{
            populateTransactionsMap();
            if(transactionsMap.size()==0)
                return false;
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        File file = new File(filePath);
        try{
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile, ',', CSVWriter.NO_QUOTE_CHARACTER);
            List<String[]> data = new ArrayList<>();
            for(String commitID_filePath : this.transactionsMap.keySet()){
                Set<String> cluster_IDs = transactionsMap.get(commitID_filePath);
                if(cluster_IDs.size()>10)
                    continue;
                data.add(cluster_IDs.toArray(String[]::new));
            }
            writer.writeAll(data);
            writer.close();
            if(data.size()==0) {
                System.out.println("transaction.csv has **** 0 **** row!");
                return false;
            }
            else {
                System.out.println("transaction.csv has **** "+data.size()+" **** rows!");
                return true;
            }
        }catch(IOException e){e.printStackTrace();}
        return false;
    }

}
