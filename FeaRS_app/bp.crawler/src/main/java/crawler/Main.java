package crawler;

import db.PsqlDB;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args){
        String dburl = args[0];
        String dbname = args[1];
        String dbuser = args[2];
        String password = (args.length>3) ? args[3] : "";

        PsqlDB.getInstance().connect(dburl, dbname, dbuser, password);

        RepositoriesCrawler crawler = new RepositoriesCrawler();

        LocalDateTime lastRun = PsqlDB.getInstance().getLastRun();
        LocalDateTime today = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        long hours = ChronoUnit.HOURS.between(lastRun, today);

        LocalDateTime endDate = today;
        final int HH = 3*30*24;
        if (hours > HH) {
            endDate = lastRun.plusHours(HH);
        }

        System.out.println("Crawling FROM " + lastRun+ " TO "+ endDate+" ...");
        crawler.crawlGithubAPI(lastRun, endDate);
        System.out.println("Crawling Finished upto " + endDate);

        PsqlDB.getInstance().insertHistoryRecord(endDate);
        PsqlDB.getInstance().disconnect();
    }
}
