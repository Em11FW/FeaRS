package similarity;

import bean.Method;
import ir.Preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Corpus {
    public Map<Integer, Map<String, Double>> corpusIRTokens = new HashMap<>();
    public Map<Integer, String[]> corpusAndroidTokens = new HashMap<>();
    public Map<Integer, Integer> methodTokens = new HashMap<>();
    public Corpus(){}
    public Corpus(Preprocessing preprocessing, ArrayList<Method> methods){
        for(Method method: methods){
            String cleaned = preprocessing.CleanText(method.getCode());
            String[] androidSplit = preprocessing.SplitForAndroidSim(cleaned);
            Map<String, Double> termsInTheDocument = preprocessing.SplitAndTokenize(cleaned);
            corpusIRTokens.put(method.getId(), termsInTheDocument);
            corpusAndroidTokens.put(method.getId(), androidSplit);
            methodTokens.put(method.getId(), method.getNTokens());
        }
    }
}
