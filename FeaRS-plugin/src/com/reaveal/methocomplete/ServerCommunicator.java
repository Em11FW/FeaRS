package com.reaveal.methocomplete;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiMethod;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServerCommunicator {

    static final String ServerAddress_Base = "https://fears.si.usi.ch/api";


    public static String SendNewMethodsToServerAndRetrieveSuggestions(Set<String> newMethodsSet, int threshold) {
        try {
            String data = CreateRequestJson(threshold, newMethodsSet);
//          String urlParameters =  "fName=" + URLEncoder.encode("???", "UTF-8") + "&lName= .... ";

            // Create connection
            String targetURL = ServerAddress_Base + "/recommendation";
            URL url = new URL(targetURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json"); //"application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            conn.setRequestProperty("Content-Language", "en-US");
            conn.setRequestProperty("charset", "utf-8");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true); // EA XP: Implicitly set the request method to POST. Solution: https://stackoverflow.com/questions/8760052


            //Send request
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            String response_str = response.toString();
            return response_str;

        } catch (IOException e) {
            OddsAndEnds.showInfoBalloon("Methocomplete Plugin", "Connecting to server failed");
            e.printStackTrace();
            return String.format("{\"result\": -1, \"errorDescription\":\"%s\" }", e.toString());
        }
    }



    static private String CreateRequestJson(int threshold, Set<String> newMethodsSet){
        String dataStr = "{\"error\": \"Failed to create request JSON\"}";
        JSONObject request = new JSONObject();
        try {
            request.put("threshold", threshold);
            List<JSONObject> newMethodsArray = new ArrayList<>();
            for (String method_text : newMethodsSet) {
                JSONObject item = new JSONObject();
                item.put("methodFullBody", method_text);
//                item.put("class", m.getClass());
//                item.put("body", m.getBody().getText());
                newMethodsArray.add(item);
            }
            request.put("newMethods", newMethodsArray);

        } catch (JSONException e) {
            e.printStackTrace();
            request = null;
        }

        if(request!=null)
            dataStr = request.toString();
        return dataStr;
    }

}
