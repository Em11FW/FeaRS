package rec;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import vis.RealTimeSimilarity;

@RestController

public class RecommendationController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }


    @RequestMapping(value = "/recommendation", method = RequestMethod.POST)
    public Map<String, Object> recommendationPost (@RequestBody String request) throws IOException, JSONException {
        RealTimeSimilarity rts = new RealTimeSimilarity();
        Set<String> lefts = new HashSet<>();
        Set<String> leftsBody = new HashSet<>();
        String left = "";
        String error = "";

        JSONObject json = new JSONObject(request);
        Map<String, String> methodSignatures = new HashMap<>();
        int sensit = json.getInt("threshold");
        rts.readArules(sensit);
        JSONArray methods = new JSONArray(json.get("newMethods").toString());
        for (int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);
            String methodbody = method.getString("methodFullBody");
            String methodSignature = null;
            String[] data = methodbody.split("\\(");
            String[] before = data[0].split(" ");
            String methodName = before[before.length - 1];
            String[] after = data[1].split("\\)");
            String[] params = after[0].split(",");
            methodName = methodName + "(";
            for (int j = 0; j < params.length; j++){
                if (j == params.length - 1) {
                    methodName = methodName + params[0].split(" ")[0];
                }
                else {
                    methodName = methodName + params[0].split(" ")[0] + ",";
                }
            }
            methodName = methodName + ")";
            left = Integer.toString(rts.computeSimilarity(methodbody, sensit));
            if (!left.equals("-1")) {
                lefts.add(left);
                leftsBody.add(methodbody);
                methodSignatures.putIfAbsent(left, methodName);
            }
        }
        try (FileWriter file = new FileWriter("NewRequest.json")) {
            file.write("new request:");
            file.write(request);
            file.write("extracted methodbody:" + leftsBody);
            file.write("matched cluster:" + lefts);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        String output = "";

        Map<Set<String>, Set<String>> finalRecommendations = new HashMap<>();
        Map<Set<String>, Set<String>> matchedSignatures = new HashMap<>();
        Map<Set<String>, List<String>> EndRecommendations = new HashMap<>();
        if (!lefts.isEmpty()) {
            finalRecommendations = new HashMap<>(rts.getAllRecommendations(lefts));
            for (Set<String> leftTmp : finalRecommendations.keySet()) {
                Set<String> sigs = new HashSet<>();
                for (String l: leftTmp){
                    sigs.add(methodSignatures.get(l));
                }
                List<String> newRight = new ArrayList<>(rts.getRightMethodsWithLinks(finalRecommendations.get(leftTmp)));
                EndRecommendations.putIfAbsent(sigs, newRight);
            }
        }
        JsonSampleCreator jsc = new JsonSampleCreator();

        return jsc.sample_map_creator(EndRecommendations, error);

    }

}
