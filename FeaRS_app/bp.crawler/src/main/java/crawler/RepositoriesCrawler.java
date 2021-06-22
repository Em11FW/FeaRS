package crawler;

import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import db.PsqlDB;


public class RepositoriesCrawler {

    final static int MIN_STARS=10;
    final static String SEARCH_URL_PREFIX = String.format("https://api.github.com/search/repositories?q=language:java+is:public+stars:%%3E%d+pushed:",MIN_STARS);

    private boolean containsAndroidCode(String fullName){
        DelayMaker.threadWaitRandom();
        System.out.format("\t\tChecking if Android\t");
        JSONObject jsonBody = HTTPUtility.getInstance().sendGetRequest("https://api.github.com/search/code?q=apply+plugin:+%27com.android.application%27+in:file+filename:build+extension:gradle+repo:"+fullName);
        boolean res =  ((jsonBody!=null)&&((int)jsonBody.get("total_count")>0));
        if(res)
            System.out.format("YES\n");
        else
            System.out.format("no\n");
        return res;
    }

    private void recurseWithSplitDatetimeInterval(LocalDateTime initDate, LocalDateTime endDate){
        if((endDate.toEpochSecond(ZoneOffset.UTC) - initDate.toEpochSecond(ZoneOffset.UTC))<1){
            System.out.println("Interval "+initDate+"-"+endDate+" is less than one second but has more than 1000 items.");
            return;
        }
        LocalDateTime midDatetime = DateInterval.getInstance().getMiddleDatetime(initDate, endDate);
        crawlGithubAPI(initDate, midDatetime);
        crawlGithubAPI(midDatetime.plusSeconds(1), endDate);
    }

    private void loopOverResultItems(JSONObject jsonBody){

        JSONArray jsonRepositoriesArray = jsonBody.getJSONArray("items");
        int nAllRepos = jsonRepositoriesArray.length();
        for (int i = 0; i < nAllRepos; i++) {
            String fullName = (String) jsonRepositoriesArray.getJSONObject(i).get("full_name");
            System.out.format("\tRepo %s/%s: %s",(i+1),nAllRepos, fullName);
            String defaultBranch = (String) jsonRepositoriesArray.getJSONObject(i).get("default_branch");
            if(containsAndroidCode(fullName)){
                PsqlDB.getInstance().insertRepository(fullName, defaultBranch);
                System.out.format("\tRepo %s/%s: %s [ADDED]\n",(i+1),nAllRepos, fullName);
            }
        }
    }

    private void loopOverResultPages(LocalDateTime initDate, LocalDateTime endDate, int totalCount) {
        int numberOfPages = 1;
        if(totalCount > 100) {
            numberOfPages = (int) Math.ceil(totalCount / 100.0);
        }
        for(int i = 1 ; i <= numberOfPages; i++){
            System.out.format("\tPage %s/%s\n", i, numberOfPages);
            DelayMaker.threadWaitRandom();
            loopOverResultItems(HTTPUtility.getInstance().sendGetRequest(SEARCH_URL_PREFIX+initDate+".."+endDate+"&per_page=100&page="+i));
        }
    }

    public void crawlGithubAPI(LocalDateTime initDate, LocalDateTime endDate){
        DelayMaker.threadWaitRandom();
        System.out.println("URL REPOS: "+SEARCH_URL_PREFIX+initDate+".."+endDate+"&per_page=100");
        JSONObject jsonBody = HTTPUtility.getInstance().sendGetRequest(SEARCH_URL_PREFIX+initDate+".."+endDate+"&per_page=100");
        if(jsonBody != null){
            int totalCount = (int)jsonBody.get("total_count");
            System.out.format("Crawling Java repos <%s - %s> --> %s results\n", initDate, endDate, totalCount);
            if(totalCount==0)
                return;
            else if(totalCount > 1000){
                System.out.println("More than 1000 repos, so splitting time interval...");
                recurseWithSplitDatetimeInterval(initDate, endDate);
            } else{
                loopOverResultPages(initDate, endDate, totalCount);
            }
        }

    }

}
