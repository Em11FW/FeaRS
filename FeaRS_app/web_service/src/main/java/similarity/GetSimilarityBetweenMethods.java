package similarity;

import ir.Preprocessing;
import ir.VSM_old;
import ir.VSM;
import utility.Utilities;
import bean.Method;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class GetSimilarityBetweenMethods {

    private Corpus corpus;
    private Preprocessing preprocessing;
    private double currentSimilarity;

    public GetSimilarityBetweenMethods() throws IOException {
        preprocessing = new Preprocessing(
                false,
                false,
                false,
                Utilities.englishStopwordPath,
                Utilities.javaStopwordPath,
                Utilities.androidAPIPath,
                Utilities.androidConstantsPath);
        corpus = new Corpus();
    }

    public void createClustersCorpus(double clusterThreshold) throws IOException {
        Connection c = null;
        String dbhost = System.getenv("DBSERVER_NAME");
        String dbport = System.getenv("DBSERVER_PORT");
        String dbname = System.getenv("POSTGRES_DB");
        String dbuser = System.getenv("POSTGRES_USER");
        String dbpassword = System.getenv("POSTGRES_PASSWORD");
        String url = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+dbname;
        try {
            c = DriverManager.getConnection(url, dbuser, dbpassword);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        String sql1 = "SELECT * from clusters" + Math.round(clusterThreshold * 100);
        try (Statement stmt1 = c.createStatement();
             ResultSet rs1 = stmt1.executeQuery(sql1)) {
            while (rs1.next()) {
                Map<String, Double> cp = new HashMap<>();
                String cluster_id = rs1.getString("cluster_id");
                String method = rs1.getString("methodBody");
                String cleaned = preprocessing.CleanText(method);
                String[] androidSplit = preprocessing.SplitForAndroidSim(cleaned);
                Map<String, Double> termsInTheDocument = preprocessing.SplitAndTokenize(cleaned);
                corpus.corpusIRTokens.put(Integer.valueOf(cluster_id), termsInTheDocument);
                corpus.corpusAndroidTokens.put(Integer.valueOf(cluster_id), androidSplit);
                corpus.methodTokens.put(Integer.valueOf(cluster_id), new StringTokenizer(method).countTokens());
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createArulesCorpus(double clusterThreshold, String arule_id) throws IOException {
        Connection c = null;
        String dbhost = System.getenv("DBSERVER_NAME");
        String dbport = System.getenv("DBSERVER_PORT");
        String dbname = System.getenv("POSTGRES_DB");
        String dbuser = System.getenv("POSTGRES_USER");
        String dbpassword = System.getenv("POSTGRES_PASSWORD");
        String url = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+dbname;
        try {
            c = DriverManager.getConnection(url, dbuser, dbpassword);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        String sql1 = "SELECT * from clusters";
        try (Statement stmt1 = c.createStatement();
             ResultSet rs1 = stmt1.executeQuery(sql1)) {
            while (rs1.next()) {
                Map<String, Double> cp = new HashMap<>();
                String cluster_id = rs1.getString("id");
                String method = rs1.getString("centroid_body");
                String cleaned = preprocessing.CleanText(method);
                String[] androidSplit = preprocessing.SplitForAndroidSim(cleaned);
                Map<String, Double> termsInTheDocument = preprocessing.SplitAndTokenize(cleaned);
                corpus.corpusIRTokens.put(Integer.valueOf(cluster_id), termsInTheDocument);
                corpus.corpusAndroidTokens.put(Integer.valueOf(cluster_id), androidSplit);
                corpus.methodTokens.put(Integer.valueOf(cluster_id), new StringTokenizer(method).countTokens());
            }
            c.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Integer getSimilarityWithClusters(Map<String, Double> idf, Method newMethod, double clusterThreshold){
        Map<Integer, Double> similarities = new HashMap<>();
        boolean containsAndroidSpecificTerms = false;
        double a = newMethod.getNTokens();
        if ( a < 6) {
            return -1;
        }
        else {
            for (String term : newMethod.getAndroidSplit()) {
                if (preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)) {
                    containsAndroidSpecificTerms = true;
                    break;
                }
            }

            for (int key : corpus.corpusAndroidTokens.keySet()) {
                boolean containsAndroidSpecificTerms1 = false;
                double b = corpus.methodTokens.get(key);
                if (a / b < 0.7 || b / a < 0.7) {
                    continue;
                } else {
                    for (String term : corpus.corpusAndroidTokens.get(key)) {
                        if (preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)) {
                            containsAndroidSpecificTerms1 = true;
                            break;
                        }
                    }
                    if (containsAndroidSpecificTerms != containsAndroidSpecificTerms1) {
                        double overallSimilarity = 0;
                    } else if (!containsAndroidSpecificTerms) {
                        double overallSimilarity = VSM_old.computeTextualSimilarity(newMethod.getPreprocessedCode(), corpus.corpusIRTokens.get(key));
                        if (overallSimilarity >= clusterThreshold) {
                            similarities.putIfAbsent(key, overallSimilarity);
                        }
                    } else {
                        double androidSimilarity = VSM_old.getAndroidSimilarity(newMethod.getAndroidSplit(), corpus.corpusAndroidTokens.get(key), preprocessing);
                        if (androidSimilarity == 0) {
                            double overallSimilarity = 0;
                        } else {
                            double textualSimilarity = VSM_old.computeTextualSimilarity(newMethod.getPreprocessedCode(), corpus.corpusIRTokens.get(key));
                            double overallSimilarity = (0.6 * textualSimilarity) + (0.4 * androidSimilarity);
                            if (overallSimilarity >= clusterThreshold) {
                                similarities.putIfAbsent(key, overallSimilarity);
                            }
                        }

                    }
                }
            }
            if (similarities.isEmpty()) {
                setCurrentSimilarity(0.0D);
                return -1;
            } else {
                int maxIndex = -1;
                double max = 0;
                for (int key : similarities.keySet()) {
                    if (similarities.get(key) > max) {
                        max = similarities.get(key);
                        maxIndex = key;
                    }
                }
                setCurrentSimilarity(max);
                return maxIndex;
            }
        }
    }

    public Integer getSimilarityWithClusters(Method newMethod, double clusterThreshold){
        Map<Integer, Double> similarities = new HashMap<>();
        boolean containsAndroidSpecificTerms = false;
        double a = newMethod.getNTokens();
        if ( a < 6) {
            return -1;
        }
        else {
            for (String term : newMethod.getAndroidSplit()) {
                if (preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)) {
                    containsAndroidSpecificTerms = true;
                    break;
                }
            }

            for (int key : corpus.corpusAndroidTokens.keySet()) {
                boolean containsAndroidSpecificTerms1 = false;
                double b = corpus.methodTokens.get(key);
                if (a / b < 0.7 || b / a < 0.7) {
                    continue;
                } else {
                    for (String term : corpus.corpusAndroidTokens.get(key)) {
                        if (preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)) {
                            containsAndroidSpecificTerms1 = true;
                            break;
                        }
                    }
                    if (containsAndroidSpecificTerms != containsAndroidSpecificTerms1) {
                        double overallSimilarity = 0;
                    } else if (!containsAndroidSpecificTerms) {
                        double overallSimilarity = VSM_old.computeTextualSimilarity(newMethod.getPreprocessedCode(), corpus.corpusIRTokens.get(key));
                        if (overallSimilarity >= clusterThreshold) {
                            similarities.putIfAbsent(key, overallSimilarity);
                        }
                    } else {
                        double androidSimilarity = VSM_old.getAndroidSimilarity(newMethod.getAndroidSplit(), corpus.corpusAndroidTokens.get(key), preprocessing);
                        if (androidSimilarity == 0) {
                            double overallSimilarity = 0;
                        } else {
                            double textualSimilarity = VSM_old.computeTextualSimilarity(newMethod.getPreprocessedCode(), corpus.corpusIRTokens.get(key));
                            double overallSimilarity = (0.6 * textualSimilarity) + (0.4 * androidSimilarity);
                            if (overallSimilarity >= clusterThreshold) {
                                similarities.putIfAbsent(key, overallSimilarity);
                            }
                        }

                    }
                }
            }
            if (similarities.isEmpty()) {
                setCurrentSimilarity(0.0D);
                return -1;
            } else {
                int maxIndex = -1;
                double max = 0;
                for (int key : similarities.keySet()) {
                    if (similarities.get(key) > max) {
                        max = similarities.get(key);
                        maxIndex = key;
                    }
                }
                setCurrentSimilarity(max);
                return maxIndex;
            }
        }
    }

    private void setCurrentSimilarity(double currentSimilarity) { this.currentSimilarity = currentSimilarity; }
    public double getCurrentSimilarity(){ return currentSimilarity; }

    public static Map<String, Double> FixMissingIDFTerms(Map<String, Double> normalizedText, Map<String,Double> idf) {
        Iterator var1 = normalizedText.keySet().iterator();
        double NEW_WORD_IDF_VALUE = Math.log(2020163.0D);
        Map<String, Double> idfWithNewWord = new HashMap<>(idf);

        while(var1.hasNext()) {
            String key = (String)var1.next();
            if (!idfWithNewWord.containsKey(key)) {
                idfWithNewWord.put(key, NEW_WORD_IDF_VALUE);
                System.out.println("WARNING: missing token in idf dataset: " + key);
            }
        }

        return idfWithNewWord;

    }

    public static double[][] getSimilarity(Map<String, Double> idf, Map<Integer, Map<String, Double>> corpus, Preprocessing preprocessing){
        double[][] similarities = new double[corpus.size()][corpus.size()];

        for(int i=0; i<corpus.size()-1; i++){
            for(int j=i+1; j<corpus.size(); j++){
                boolean containsAndroidSpecificTerms = false;
                double irSimilarity = VSM.computeTextualSimilarity(corpus.get(i), corpus.get(j), idf);
                for(String term: corpus.get(i).keySet()){
                    if(preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)){
                        System.out.println(term);
                        containsAndroidSpecificTerms = true;
                        break;
                    }
                }

                if(!containsAndroidSpecificTerms){
                    for(String term: corpus.get(j).keySet()){
                        if(preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)){
                            System.out.println(term);
                            containsAndroidSpecificTerms = true;
                            break;
                        }
                    }
                }

                double androidSimilarity = 0.0;
                double overallSimilarity = irSimilarity;
                if(containsAndroidSpecificTerms){
                    androidSimilarity = VSM.getAndroidSimilarity(corpus.get(i), corpus.get(j), preprocessing);
                    overallSimilarity = (0.5*irSimilarity) + (0.5*androidSimilarity);
                }

                System.out.println(containsAndroidSpecificTerms);
                System.out.println(i + "-" + j + " IR: " + irSimilarity + "   AS: " + androidSimilarity);

                similarities[i][j] = overallSimilarity;

            }
        }

        return similarities;
    }

    public static Integer getSimilarityWithClusters(Map<String, Double> idf, Map<String, Double> newMethod, Map<Integer, Map<String, Double>> corpus, Preprocessing preprocessing){
        Map<Integer, Double> similarities = new HashMap<>();
        boolean containsAndroidSpecificTerms = false;
        for (String term : newMethod.keySet()) {
            if (preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)) {
                containsAndroidSpecificTerms = true;
                break;
            }
        }

        for (int key:corpus.keySet()) {
            boolean containsAndroidSpecificTerms1 = false;
            for (String term : corpus.get(key).keySet()) {
                if (preprocessing.androidAPIs.contains(term) || preprocessing.androidClasses.contains(term) || preprocessing.androidConstants.contains(term)) {
                    containsAndroidSpecificTerms1 = true;
                    break;
                }
            }
            if (containsAndroidSpecificTerms != containsAndroidSpecificTerms1) {
                double overallSimilarity = 0;
            } else if (!containsAndroidSpecificTerms) {
                double overallSimilarity = VSM.computeTextualSimilarity(newMethod, corpus.get(key), idf);
                if (overallSimilarity >= 0.65) {
                    similarities.putIfAbsent(key,overallSimilarity);
                }
            } else {
                double androidSimilarity = VSM.getAndroidSimilarity(newMethod, corpus.get(key), preprocessing);
                if (androidSimilarity == 0) {
                    double overallSimilarity = 0;
                } else {
                    double textualSimilarity = VSM.computeTextualSimilarity(newMethod, corpus.get(key), idf);
                    double overallSimilarity = (0.5 * textualSimilarity) + (0.5 * androidSimilarity);
                    if (overallSimilarity >= 0.65) {
                        similarities.putIfAbsent(key,overallSimilarity);
                    }
                }

            }
        }
        if(similarities.isEmpty()){
            return -1;
        }
        else{
            int maxIndex = -1;
            double max = 0;
            for(int key:similarities.keySet()){
                if(similarities.get(key) > max){
                    max = similarities.get(key);
                    maxIndex = key;
                }
            }
            return maxIndex;
        }



    }

}
