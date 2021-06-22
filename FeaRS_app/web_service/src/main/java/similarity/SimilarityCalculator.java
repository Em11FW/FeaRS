package similarity;

import ir.Preprocessing;
import ir.VSM;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimilarityCalculator implements Runnable{

    private Map<Integer, Map<String, Double>> corpus;
    private ArrayList<String> words = null;
    private PrintWriter pw = null;
    private List<Set<String>> documents = null;
    private int start;
    private int end;
    private Preprocessing preprocessing;
    private Map<String, Double> idf;
    private List<Integer> methodTokens;

    public SimilarityCalculator(Map<Integer, Map<String, Double>> corpus, PrintWriter pw, Preprocessing preprocessing, Map<String, Double> idf, List<Integer> methodTokens){
        this.corpus = corpus;
        this.pw = pw;
        this.start = 0;
        this.end = corpus.size();
        this.preprocessing = preprocessing;
        this.idf = idf;
        this.methodTokens = methodTokens;
    }

    @Override
    public void run() {
        for(int i=start; i<end; i++) {
            System.out.println(i + ":" + end);
            boolean containsAndroidSpecificTerms = false;
            double a = methodTokens.get(i);
            if (a < 6) {
                double overallSimilarity = 0;
            }
            else {
                //Check if the documents contain android-specific terms
                for (String term : corpus.get(i).keySet()) {
                    if (preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)) {
                        containsAndroidSpecificTerms = true;
                        break;
                    }
                }
                for (int j = i + 1; j < corpus.size(); j++) {
                    double b = methodTokens.get(j);
                    if (a / b < 0.7 || b / a < 0.7) {
                        double overallSimilarity = 0;
                    } else {
                        boolean containsAndroidSpecificTerms1 = false;
                        for (String term : corpus.get(j).keySet()) {
                            if (preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)) {
                                //System.out.println(term);
                                containsAndroidSpecificTerms1 = true;
                                break;
                            }
                        }
                        if (containsAndroidSpecificTerms != containsAndroidSpecificTerms1) {
                            double overallSimilarity = 0;
                        } else if (!containsAndroidSpecificTerms) {
                            double overallSimilarity = VSM.computeTextualSimilarity(corpus.get(i), corpus.get(j), idf);
                            if (overallSimilarity >= 0.5) {
                                pw.println(i + "," + j + "," + overallSimilarity + "," + overallSimilarity + "," + 0);
                            }
                        } else {
                            double androidSimilarity = VSM.getAndroidSimilarity(corpus.get(i), corpus.get(j), preprocessing);
                            if (androidSimilarity == 0) {
                                double overallSimilarity = 0;
                            } else {
                                double textualSimilarity = VSM.computeTextualSimilarity(corpus.get(i), corpus.get(j), idf);
                                double overallSimilarity = (0.5 * textualSimilarity) + (0.5 * androidSimilarity);
                                if (overallSimilarity >= 0.5) {
                                    pw.println(i + "," + j + "," + overallSimilarity + "," + textualSimilarity + "," + androidSimilarity);
                                }
                            }

                        }
                    }

                }
            }
        }
        pw.close();

    }

}

