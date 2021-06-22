package arule;

import analyser_db.PsqlDB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ARuleMain {

    public static void main(String[] args){
        double support = Double.parseDouble(args[0]);
        double confidence = Double.parseDouble(args[1]);
        int maxLength = Integer.parseInt(args[2]);
        String url = args[3];
        String dbname = args[4];
        String dbuser = args[5];
        String password = (args.length>6) ? args[6] : "";
        PsqlDB.getInstance().connect(url,dbname,dbuser,password);
        System.out.println("Association rule discovery...");
        boolean shouldContinue = TransactionsCSVWriter.getInstance().writeTransactionsCSV("./transactions.csv");
        if(shouldContinue==false)
        {
            System.out.println("Nothing to process. Returning without touching ARules Table.");
            return;
        }
        String ARuleFile_1 = "", ARuleFile_2= "", ARuleFile_3 = "";
        try{
            System.out.println("\tCreating three set of rules ...");
            ARuleFile_1 = RInteractionHelper.getInstance().executeARule(1);
            System.out.println("\t\t[DONE 1/3]");
            ARuleFile_2 = RInteractionHelper.getInstance().executeARule(2);
            System.out.println("\t\t[DONE 2/3]");
            ARuleFile_3 = RInteractionHelper.getInstance().executeARule(3);
            System.out.println("\t\t[DONE 3/3]");

            System.out.format("\tCleaning ARule table ...");
            PsqlDB.getInstance().cleanARulesTable();
            System.out.format("\t [DONE]\n");

            System.out.println("\tInserting rules ...");
            long db_startID = 0;
            db_startID = AssociationsFileParser.getInstance().parseARuleFileAndStoreInDatabase(Paths.get(ARuleFile_1), 1, db_startID);
            db_startID = AssociationsFileParser.getInstance().parseARuleFileAndStoreInDatabase(Paths.get(ARuleFile_2), 2, db_startID);
            AssociationsFileParser.getInstance().parseARuleFileAndStoreInDatabase(Paths.get(ARuleFile_3), 3, db_startID);
            System.out.println("\tInserting rules [DONE]");
        }catch(Exception e){
            System.err.println("Cannot create association rules");
            e.printStackTrace();
        }
        try {
            Files.delete(Paths.get("./transactions.csv"));
            Files.delete(Paths.get(ARuleFile_1));
            Files.delete(Paths.get(ARuleFile_2));
            Files.delete(Paths.get(ARuleFile_3));
        }catch(IOException e){
            System.err.println("Cannot delete files created during arules creation");
            e.printStackTrace();
        }
        System.out.println("Association rule discovery ended.");
        PsqlDB.getInstance().disconnect();
    }
}
