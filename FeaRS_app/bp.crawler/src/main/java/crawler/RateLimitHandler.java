package crawler;

import org.json.JSONObject;
import java.time.Instant;

public class RateLimitHandler {

    private static RateLimitHandler rateLimitHandlerInstance = null;
    private  JSONObject responseBody = null;

    private RateLimitHandler(){}

    public static RateLimitHandler getInstance(){
        if(rateLimitHandlerInstance == null){
            rateLimitHandlerInstance = new RateLimitHandler();
        }
        return rateLimitHandlerInstance;
    }

    private long computeSecondsBeforeRateLimitReset(int resetTime){
        long now = Instant.now().getEpochSecond();
        return Math.max(0, (resetTime-now));
    }

    public void setResponseBody(JSONObject responseBody){
        this.responseBody = responseBody;
    }

    public long getSecondsBeforeRateLimitReset(){
        long secondsBeforeRateLimitReset = -1;
        if(responseBody != null){
            int remaining = (int)responseBody.getJSONObject("resources").getJSONObject("search").get("remaining");
            if(remaining>0)
                return 0;
            int resetTime = (int)responseBody.getJSONObject("resources").getJSONObject("search").get("reset");
            secondsBeforeRateLimitReset = computeSecondsBeforeRateLimitReset(resetTime);
        }
        return secondsBeforeRateLimitReset;
    }


}
