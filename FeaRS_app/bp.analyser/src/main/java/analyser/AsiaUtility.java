package analyser;

import api.ASIASim;
import api.LoadStartingData;
import bean.RetrievedDocument;
import ir.Preprocessing;

public class AsiaUtility {
    private static AsiaUtility asiaUtilityInstance = null;
    private AsiaUtility() throws Exception{
        Preprocessing preprocessing = LoadStartingData.getPreprocessing(
                "/usr/local/fears-app/resources/asia-resources/stopword/stop-words-english.txt",
                "/usr/local/fears-app/resources/asia-resources/stopword/stop-words-java.txt",
                "/usr/local/fears-app/resources/asia-resources/androidAPI/android-API.csv",
                "/usr/local/fears-app/resources/asia-resources/androidAPI/android_constants.txt");
        ASIASim.Initialize(preprocessing, "/usr/local/fears-app/resources/asia-resources/idf-code-only-entire-dataset");
    }

    public static AsiaUtility getInstance(){
        if(asiaUtilityInstance==null){
            try{
                asiaUtilityInstance = new AsiaUtility();
            }catch (Exception e ){
                System.err.println("Cannot initialize AsiaUtility");
                e.printStackTrace();
            }
        }
        return asiaUtilityInstance;
    }

    public Double computeSimilarity(String codeSnippet1, String codeSnippet2) throws Exception {
        RetrievedDocument res = ASIASim.CalculateSim(codeSnippet1,codeSnippet2);
        return res.getSimilarityScore();
    }
}
