package ir;

import com.google.common.collect.Sets;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;

public class VSM {

    private static double vsm(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> both = Sets.newHashSet(v1.keySet());
        both.addAll(v2.keySet());

        double[] d1 = new double[both.size()];
        double[] d2 = new double[both.size()];

        int i = 0;
        for (String key : both) {
            d1[i] = 0;
            d2[i] = 0;

            if (v1.containsKey(key))
                d1[i] = v1.get(key);

            if (v2.containsKey(key))
                d2[i] = v2.get(key);

            i++;
        }

        RealVector vector1 = new ArrayRealVector(d1);
        RealVector vector2 = new ArrayRealVector(d2);

        try {
            return vector1.cosine(vector2);
        } catch (MathArithmeticException e) {
            return Double.NaN;
        }
    }

    private static Map<String, Double> applyTfIdf(Map<String, Double> text, Map<String, Double> idf){
        Map<String, Double> map = new HashMap<>();

        for (String key : text.keySet()) {
            if(idf.keySet().contains(key)) {
                map.put(key, idf.get(key) * text.get(key));
            }
            else{
                map.put(key, Math.log(3245939) * text.get(key));
            }
        }

        return map;
    }


    public static double computeTextualSimilarity(Map<String, Double> first, Map<String, Double> second, Map<String, Double> idf){

        HashMap<String, Double> firstDocument = (HashMap<String, Double>) applyTfIdf(first, idf);
        HashMap<String, Double> secondDocument = (HashMap<String, Double>) applyTfIdf(second, idf);

        return VSM.vsm(firstDocument, secondDocument);
    }

    public static double getAndroidSimilarity(Map<String, Double> first, Map<String, Double> second, Preprocessing preprocessing){
        Set<String> both = Sets.newHashSet(first.keySet());
        both.addAll(second.keySet());

        double overall = 0.0;
        double inCommon = 0.0;

        for(String term: both){
            if(preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)){
                overall++;
                if(first.keySet().contains(term) && second.keySet().contains(term))
                    inCommon++;
            }
        }

        if(overall > 0.0)
            return inCommon/overall;
        else
            return -1.0;
    }


    public static Map<String, Double> computeIDF(Collection<Map<String, Double>> strings){
        List<Set<String>> documents = new ArrayList<>();
        ArrayList<String> words = new ArrayList<String>();

        for (Map<String, Double> doc : strings) {
            Set<String> document = new HashSet<>();
            for (String s : doc.keySet()) {
                document.add(s);
                if(!words.contains(s))
                    words.add(s);
            }
            documents.add(document);
        }

        return computeIDF(words, documents);
    }

    private static Map<String, Double> computeIDF(ArrayList<String> words, List<Set<String>> documents) {
        Map<String, Double> idf = new HashMap<String, Double>();

        for (String word : words) {
            double occurrences = 0;
            for (Set<String> document : documents) {
                if (document.contains(word))
                    occurrences++;
            }
            double value = (((double)documents.size()) / occurrences);
            idf.put(word, Math.log(value));
        }

        return idf;
    }

}
