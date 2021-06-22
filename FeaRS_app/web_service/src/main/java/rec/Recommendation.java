package rec;

import java.lang.reflect.Array;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class Recommendation {

    private final JSONObject recommendations;

    public Recommendation(JSONObject recommendations) {
        this.recommendations = recommendations;
    }
}
