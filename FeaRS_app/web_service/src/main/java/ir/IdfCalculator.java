package ir;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IdfCalculator implements Runnable{

    private ArrayList<String> words = null;
    private PrintWriter pw = null;
    private List<Set<String>> documents = null;
    private int start;
    private int end;

    public IdfCalculator(ArrayList<String> words, List<Set<String>> documents, PrintWriter pw, int start, int end){
        this.words = words;
        this.documents = documents;
        this.pw = pw;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        System.out.println("Started " + start + " - " + end);

        if(end >= words.size())
            end = words.size();

        for(int i=start; i<end; i++){
            String word = words.get(i);
            double occurrences = 0;
            for (Set<String> document : documents) {
                if (document.contains(word))
                    occurrences++;
            }
            double value = (((double)documents.size()) / occurrences);
            pw.println(word + "," + Math.log(value));
        }

        pw.close();
        System.out.println(start + " - " + end + " DONE.");

    }

}
