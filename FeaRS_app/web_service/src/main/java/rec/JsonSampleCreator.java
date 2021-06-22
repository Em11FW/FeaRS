package rec;

import bean.Method;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JsonSampleCreator {
    public JSONObject sample_json_creator(Set<String> lefts, Set<String> rights, String errorMsg) throws JSONException {


        JSONArray recommendations = new JSONArray();

        JSONObject recommendation = new JSONObject();

        recommendation.put("based_on", lefts);
        recommendation.put("suggested", rights);
        recommendations.put(recommendation);


        JSONObject jsonRoot = new JSONObject();
        jsonRoot.put("error", errorMsg);
        jsonRoot.put("results", recommendations);
        return jsonRoot;
    }

    public Map<String, Object> sample_map_creator(Map<Set<String>, List<String>> allRecommendations, String errorMsg)
    {

        List<Map<String,Object>> recommendations = new ArrayList<>();
        Map<String, Object> jsonRoot = new HashMap<>();
        if (!allRecommendations.isEmpty()) {
            for(Set<String> lefts : allRecommendations.keySet()) {
                Map<String, Object> recommendation = new HashMap<>();
                List<Object> rights = new ArrayList<>();
                recommendation.put("based_on", lefts);
                for (int i = 0; i < allRecommendations.get(lefts).size(); i += 2){
                    Map<String, String> right = new HashMap<>();
                    right.put("code", allRecommendations.get(lefts).get(i));
                    right.put("source", allRecommendations.get(lefts).get(i+1));
                    rights.add(right);
                }
                recommendation.put("suggestions", rights);
                recommendations.add(recommendation);
            }
        }

        jsonRoot.put("error", errorMsg);
        jsonRoot.put("results", recommendations);


        return jsonRoot;
    }

    public void foo(){

    }


}
