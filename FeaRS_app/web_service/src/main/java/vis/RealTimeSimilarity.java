package vis;

import ir.Preprocessing;
import similarity.GetSimilarityBetweenMethods;
import utility.Utilities;
import bean.Method;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class RealTimeSimilarity {
    public Map<Integer, Map<String, Double>> corpus = new HashMap<>();
    public HashSet<String> inputMethods = new HashSet<>();
    public Map<Set<String>, Set<String>> rules = new HashMap<>();
    public Map<Set<String>, List<RightValue>> rulesNew = new HashMap<>();
    public Set<String> rightMethods = new HashSet<>();


    public Integer computeSimilarity(String method, int sensit) throws IOException {
        Preprocessing preprocessing = new Preprocessing(false, false, false,
                Utilities.englishStopwordPath,
                Utilities.javaStopwordPath,
                Utilities.androidAPIPath,
                Utilities.androidConstantsPath);
        String cleaned = preprocessing.CleanText(method);
        int nToken = new StringTokenizer(method).countTokens();
        Method newMethod = new Method(preprocessing.SplitAndTokenize(cleaned),preprocessing.SplitForAndroidSim(cleaned),method,nToken);
        GetSimilarityBetweenMethods gsbm = new GetSimilarityBetweenMethods();
        List<String> aruleFilenames = new ArrayList<>();
        aruleFilenames.add("arules-8e-06-0.8-3");
        aruleFilenames.add("arules-8e-06-0.65-2");
        aruleFilenames.add("arules-8e-06-0.5-2");
        String s = aruleFilenames.get(sensit);
        gsbm.createArulesCorpus(0.90, s);
        return gsbm.getSimilarityWithClusters(newMethod, 0.90);
    }

    public Map<Set<String>, Set<String>> getAllRecommendations(Set<String> lefts){
        Map<Set<String>, Set<String>> rulesMatched = new HashMap<>();
        List<TwoEndValue> leftArulesMatched = new ArrayList<>();
        for (Set<String> ruleLefts : rulesNew.keySet()) {
            Set<String> tmpMethods = new HashSet<>(lefts);
            if (lefts.containsAll(ruleLefts)) {
                String left = lefts.toString();
                tmpMethods.removeAll(ruleLefts);
                List<RightValue> tmpRights = new ArrayList<>(rulesNew.get(ruleLefts));
                for (RightValue right : tmpRights) {
                    if (tmpMethods.contains(right.rightMethod)){
                        rulesNew.get(ruleLefts).remove(right);
                    }
                    else {
                        List<String> tmplist = new ArrayList<>(ruleLefts);
                        tmplist.add(right.rightMethod);
                        leftArulesMatched.add(new TwoEndValue(tmplist, right.confidence));
                    }
                }
            }
        }
        List<List<String>> ArulesMatched = new ArrayList<>(mergeMatchedArules(leftArulesMatched));
        for(List<String> tmp : ArulesMatched){
            Set<String> tmpL = new HashSet<>(tmp.subList(0,tmp.size()-2));
            Set<String> tmpR = new HashSet<>(tmp.subList(tmp.size()-2,tmp.size()-1));
            if (!rulesMatched.keySet().contains(tmpL)){
                rulesMatched.putIfAbsent(tmpL,tmpR);
            }
            else {
                rulesMatched.get(tmpL).addAll(tmpR);
            }
        }
        return rulesMatched;

    }

    public static List<List<String>> mergeMatchedArules(List<TwoEndValue> twoEnd){
        Map<String, List<LeftValue>> rightArules = new HashMap<>();
        Map<String, LeftValue> rightArulesMerged = new HashMap<>();
        List<TwoEndValue> twoEndArules = new ArrayList<>();
        Set<TwoEndValue> twoEndValuesToRemove = new HashSet<>();
        for (TwoEndValue twoEndValue: twoEnd){
            Set<String> tmpleft = new HashSet<>(twoEndValue.twoEndMethod.subList(0,twoEndValue.twoEndMethod.size()-1));
            String tmpRight = twoEndValue.twoEndMethod.get(twoEndValue.twoEndMethod.size()-1);
            LeftValue leftValue = new LeftValue(tmpleft, twoEndValue.confidence);
            if (!rightArules.containsKey(tmpRight)){
                List<LeftValue> tmp = new ArrayList<>();
                tmp.add(leftValue);
                rightArules.putIfAbsent(tmpRight,tmp);
            }
            else{
                rightArules.get(tmpRight).add(leftValue);
            }
        }

        for (String right : rightArules.keySet()){
            double maxConfidence = 0;
            Set<String> tmpLeft = new HashSet<>();
            for (LeftValue lefts: rightArules.get(right)){
                if (lefts.confidence > maxConfidence){
                    maxConfidence = lefts.confidence;
                    tmpLeft = new HashSet<>(lefts.leftMethod);
                }
                else if (lefts.confidence == maxConfidence && lefts.leftMethod.size() < tmpLeft.size()){
                    tmpLeft = new HashSet<>(lefts.leftMethod);
                }
            }
            LeftValue leftMerged = new LeftValue(tmpLeft, maxConfidence);
            rightArulesMerged.putIfAbsent(right, leftMerged);
        }
        for (String right : rightArulesMerged.keySet()){
            List<String> twoEndMethod = new ArrayList<>(rightArulesMerged.get(right).leftMethod);
            twoEndMethod.add(right);
            TwoEndValue tmpTwoEnd = new TwoEndValue(twoEndMethod, rightArulesMerged.get(right).confidence);
            twoEndArules.add(tmpTwoEnd);
        }
        List<List<String>> results = new ArrayList<>();
        for(TwoEndValue twoEndValue: twoEndArules){
            List<String> tmp = new ArrayList<>(twoEndValue.twoEndMethod);
            tmp.add(Double.toString(twoEndValue.confidence));
            results.add(tmp);
        }
        return results;

    }

    public void createCorpus() throws IOException {
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
        String sql1 = "SELECT * from corpus090_LC";
        try (Statement stmt1 = c.createStatement();
             ResultSet rs1 = stmt1.executeQuery(sql1)) {
            while (rs1.next()) {
                Map<String, Double> cp = new HashMap<>();
                String cluster_id = rs1.getString("cluster_id");
                String method = rs1.getString("methodCorpus");
                method = method.substring(1,method.length()-1);
                String[] words = method.split(", ");
                for (String word: words){
                    String[] w = word.split("=");
                    cp.putIfAbsent(w[0],Double.valueOf(w[1]));
                }
                corpus.putIfAbsent(Integer.valueOf(cluster_id),cp);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createRules() throws IOException {
        File file1 = new File("/home/wenf/ICSE/7334-90/arules-1e-05-0.05-2");
        BufferedReader csvReader = new BufferedReader(new FileReader(file1));
        String line = null;
        while ((line = csvReader.readLine()) != null) {
            Set<String> lefts = new HashSet<>();
            if (line.contains("=>")) {
                String[] data = line.split(" ");
                String left = data[1].substring(2, data[1].length() - 1);
                String right = data[3].substring(1, data[3].length() - 2);
                lefts.add(left);

                if (!rules.keySet().contains(lefts)) {
                    Set<String> rights = new HashSet<>();
                    rights.add(right);
                    rules.putIfAbsent(lefts, rights);
                } else {
                    rules.get(lefts).add(right);
                }
            }
        }
    }

    public void readArules(int sensit) throws IOException {
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
        Map<Set<String>, List<RightValue>> arules = new HashMap<>();
        String sql1 = "SELECT * from arules where sensitivity = " + sensit;
        try (Statement stmt1 = c.createStatement();
             ResultSet rs1 = stmt1.executeQuery(sql1)) {
            while (rs1.next()) {
                java.sql.Array a1 = rs1.getArray("les");
                Long[] left_int = (Long[])a1.getArray();
                String[] left = new String[left_int.length];

                for (int i = 0; i < left_int.length; i++) {
                    left[i] = String.valueOf(left_int[i]);
                }
                Set<String> leftKey = new HashSet<>(Arrays.asList(left));
                RightValue rightValue = new RightValue(String.valueOf(rs1.getInt("res")),rs1.getDouble("confidence"));
                if (!arules.containsKey(leftKey)){
                    List<RightValue> right = new ArrayList<>();
                    right.add(rightValue);
                    arules.putIfAbsent(leftKey, right);
                }
                else {
                    arules.get(leftKey).add(rightValue);
                }

            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        rulesNew = arules;
    }

    public void getRights(List<RightValue> rights) {
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
        for (RightValue right:rights){
            String r = right.rightMethod;
            String sql1 = "SELECT * from arulesfears where cluster_id = '" + r + "'";
            try (Statement stmt1 = c.createStatement();
                 ResultSet rs1 = stmt1.executeQuery(sql1)) {
                rs1.next();
                rightMethods.add(rs1.getString("methodBody"));
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public Set<String> getRightMethods(Set<String> rights){
        Connection c = null;
        Set<String> rightsNew = new HashSet<>();
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
        for (String right:rights){
            String sql1 = "SELECT * from arulesfears where cluster_id = '" + right + "'";
            try (Statement stmt1 = c.createStatement();
                 ResultSet rs1 = stmt1.executeQuery(sql1)) {
                rs1.next();
                rightsNew.add(rs1.getString("methodBody"));
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return rightsNew;
    }

    public List<String> getRightMethodsWithLinks (Set<String> rights){
        Connection c = null;
        List<String> rightsNew = new ArrayList<>();
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
        for (String right:rights){
            String sql1 = "SELECT * from clusters where id = '" + right + "'";
            try (Statement stmt1 = c.createStatement();
                 ResultSet rs1 = stmt1.executeQuery(sql1)) {
                rs1.next();
                rightsNew.add(rs1.getString("centroid_body"));//centroid
                rightsNew.add(rs1.getString("source"));//source

            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return rightsNew;
    }

    private static class RightValue {
        private String rightMethod;
        private double confidence;
        private RightValue(String rightMethod, double confidence){
            this.rightMethod = rightMethod;
            this.confidence = confidence;
        }
    }


    private static class LeftValue {
        private Set<String> leftMethod;
        private double confidence;
        private LeftValue(Set<String> leftMethod, double confidence){
            this.leftMethod = leftMethod;
            this.confidence = confidence;
        }
    }

    private static class TwoEndValue {
        private List<String> twoEndMethod;
        private double confidence;
        private TwoEndValue(List<String> twoEndMethod, double confidence){
            this.twoEndMethod = twoEndMethod;
            this.confidence = confidence;
        }
    }

}
