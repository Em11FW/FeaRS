package arule;

import analyser_db.PsqlDB;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class AssociationsFileParser {
    private static AssociationsFileParser parserInstance = null;
    private AssociationsFileParser(){ }
    public static AssociationsFileParser getInstance(){
        if(parserInstance==null)
            parserInstance = new AssociationsFileParser();
        return parserInstance;
    }

    public long parseARuleFileAndStoreInDatabase(Path filepath, int sensitivity_level, long db_startID)throws Exception{
        List<String> lines = Files.readAllLines(filepath);
        lines.remove(0);
        System.out.println("Inserting "+lines.size()+" rules into table ....");
        List<ARuleItem> aRules = new ArrayList<>();
        for(String line: lines) {
            ARuleItem a = this.parseLine(line);
            aRules.add(a);
        }
        PsqlDB.getInstance().insertARules(aRules, sensitivity_level, db_startID);
        System.out.println("\t\t\tDONE");
        return db_startID+aRules.size();
    }
    // LES = Left End Side of association rule containing clusters IDS
    // RES = Right End Side of association rule containing cluster ID
    private ARuleItem parseLine(String line){
        double support, confidence, lift;
        long count, id, RES;
        ArrayList<Long> LES = new ArrayList<>();
        String[] words = line.split("\\s+");

        StringBuilder stringBuilder = new StringBuilder(words[0]);
        stringBuilder.deleteCharAt(0).deleteCharAt(stringBuilder.length()-1);
        id = Long.parseLong(stringBuilder.toString());
        stringBuilder.setLength(0);

        stringBuilder.append(words[1]);
        stringBuilder.deleteCharAt(0).deleteCharAt(0).deleteCharAt(stringBuilder.length()-1);
        String[] clustersIds = stringBuilder.toString().split(",");
        for (String clustersId : clustersIds) {
            LES.add(Long.parseLong(clustersId));
        }
        stringBuilder.setLength(0);

        stringBuilder.append(words[3]);
        stringBuilder.deleteCharAt(0).deleteCharAt(stringBuilder.length()-2).deleteCharAt(stringBuilder.length()-1);

        RES = Long.parseLong(stringBuilder.toString());

        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        formatSymbols.setDecimalSeparator('.');
        DecimalFormat decim = new DecimalFormat("0.00", formatSymbols);

        support = Double.parseDouble(words[4]);
        support = Double.parseDouble(decim.format(support));

        confidence = Double.parseDouble(words[5]);
        confidence = Double.parseDouble(decim.format(confidence));

        // 6: coverage

        lift = Double.parseDouble(words[7]);
        lift = Double.parseDouble(decim.format(lift));

        count = Long.parseLong(words[8]);

        return new ARuleItem(id,LES,RES,support,confidence,lift,count);
    }


    public static class ARuleItem {
        public double support, confidence, lift;
        public long count, id, RES;
        public ArrayList<Long> LES = new ArrayList<>();

        public ARuleItem(long id, ArrayList<Long> les, long res, double support, double confidence, double lift, long count) {
            this.id = id;
            this.LES.addAll(les);
            this.RES = res;
            this.support = support;
            this.confidence = confidence;
            this.lift = lift;
            this.count = count;
        }
    }
}
