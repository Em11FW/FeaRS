package crawler;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public class HTTPUtility {
    private static HTTPUtility httpUtilityInstance = null;
    private final ArrayList<String> tokens = new ArrayList<>(Arrays.asList(
            "token valid_token_1",
            "token valid_token_2"

    ));
    private String token = this.tokens.get(0);
    private int index = 0;


    private HTTPUtility(){}

    public static HTTPUtility getInstance(){
        if(httpUtilityInstance == null){
            httpUtilityInstance = new HTTPUtility();
        }
        return httpUtilityInstance;
    }

    private HttpClient createClient(){
        return HttpClient.newBuilder().connectTimeout(Duration.ofMillis(30000)).build();
    }

    private void switchToken(){
        if(this.index < this.tokens.size()-1){
            ++this.index;
        }else{
            this.index = 0;
        }
        this.token = this.tokens.get(this.index);
    }

    private HttpRequest createGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", this.token)
                .timeout(Duration.ofMillis(30000))
                .GET()
                .build();
    }

    private JSONObject getHTTPResponseBody(HttpResponse<String> response){
        JSONObject jsonBody = null;
        if(response != null){
            jsonBody = new JSONObject(response.body());
        }
        return jsonBody;
    }

    private JSONObject sendGetRateLimitRequest(){
        HttpRequest request = createGetRequest("https://api.github.com/rate_limit");
        try{
            HttpResponse response = createClient().send(request, HttpResponse.BodyHandlers.ofString());
            return getHTTPResponseBody(response);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject sendGetRequest(String url){
        HttpRequest request = createGetRequest(url);
        final int MAX_TRY = 3;
        int tryRounds = 1;
        HttpResponse<String> response;
        while(tryRounds < MAX_TRY) {
            try {
                response = createClient().send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() == 403){
                    System.err.println("Response received with status code 403: "+url);
                    System.err.println("GitHub search API request rate limit reached. Switching token.");
                    switchToken();
                    request = createGetRequest(url);
                    RateLimitHandler.getInstance().setResponseBody(sendGetRateLimitRequest());
                    long resetWait_sec = RateLimitHandler.getInstance().getSecondsBeforeRateLimitReset()*1000;
                    System.out.format("Waiting %s second to reset Rate Limit ...\n", resetWait_sec);
                    DelayMaker.threadWait(resetWait_sec/1000);
                }
                else if(response.statusCode()!= 200){
                    System.err.println("Response received with status code : " + response.statusCode());
                    DelayMaker.threadWait(2000);
                    tryRounds++;
                } else{
                    return getHTTPResponseBody(response);
                }
            } catch (HttpConnectTimeoutException e)
            {
                System.err.println("Error - Timeout: "+url);
                e.printStackTrace();
                tryRounds++;
            }
            catch (IOException | InterruptedException e) {
                System.err.println("Error: "+url);
                e.printStackTrace();
                DelayMaker.threadWait(1000);
                tryRounds++;
            }
            System.out.println("++ Trying #"+tryRounds);
        }

        System.err.println("Failed accessing url after "+MAX_TRY+"tries: "+url);
        return null;
    }


}
