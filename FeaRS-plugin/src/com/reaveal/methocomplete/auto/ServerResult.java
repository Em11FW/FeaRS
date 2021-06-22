package com.reaveal.methocomplete.auto;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServerResult {
    public List<String> based_on = new ArrayList<>();
    public List<String> codeSnippets = new ArrayList<>();
    public List<String> codeSnippetsLink = new ArrayList<>();

    ServerResult(JSONObject result_jsonObject)
    {
        if(result_jsonObject==null) {
            based_on.add("void Fake1()");
            based_on.add("void Fake2()");

            codeSnippets.add("Fake 1");
            codeSnippets.add("Fake 2");
            codeSnippets.add("Fake 3");
            codeSnippets.add("Fake 4");
            codeSnippets.add("Fake 5");
            return;
        }

        try {
            JSONArray basedOn_jsonArray = result_jsonObject.getJSONArray("based_on");
            for(int i=0; i<basedOn_jsonArray.length(); i++)
                based_on.add((String) basedOn_jsonArray.get(i));

            JSONArray suggestions_jsonArray = result_jsonObject.getJSONArray("suggestions");
            for(int i=0; i<suggestions_jsonArray.length(); i++) {
                JSONObject suggestionJsonObj = suggestions_jsonArray.getJSONObject(i);
                codeSnippets.add((String) suggestionJsonObj.get("code"));
                codeSnippetsLink.add((String) suggestionJsonObj.get("source"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
