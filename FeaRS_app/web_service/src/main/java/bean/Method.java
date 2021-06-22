package bean;

import java.util.Map;

public class Method {

    private int id;
    private String code;
    private Map<String, Double> preprocessedCode;
    private int clusterId;
    private int nTokens;
    private String[] androidSplit;

    public Method(Map<String, Double> preprocessedCode, String[] androidSplit, String code, int nToken){
        this.preprocessedCode = preprocessedCode;
        this.androidSplit = androidSplit;
        this.code = code;
        this.nTokens = nToken;
    }

    public Method(Map<String, Double> preprocessedCode, String[] androidSplit){
        this.preprocessedCode = preprocessedCode;
        this.androidSplit = androidSplit;
    }

    public Method(){}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public int getNTokens() {
        return nTokens;
    }
    public void setNTokens(int nTokens) { this.nTokens = nTokens; }
    public Map<String, Double> getPreprocessedCode() {
        return preprocessedCode;
    }
    public void setPreprocessedCode(Map<String, Double> preprocessedCode) {
        this.preprocessedCode = preprocessedCode;
    }
    public String[] getAndroidSplit() { return  androidSplit; }
    public void setAndroidSplit(String[] androidSplit) { this.androidSplit = androidSplit; }
    public int getClusterId() {
        return clusterId;
    }
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

}
