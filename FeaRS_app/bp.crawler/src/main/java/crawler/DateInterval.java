package crawler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateInterval {

    private static DateInterval dateIntervalInstance = null;

    private DateInterval(){ }

    public static DateInterval getInstance(){
        if(dateIntervalInstance == null){
            dateIntervalInstance = new DateInterval();
        }
        return dateIntervalInstance;
    }

    public LocalDateTime getMiddleDatetime(LocalDateTime initDate, LocalDateTime endDate){
        long initDateInSeconds = initDate.toEpochSecond(ZoneOffset.UTC);
        long endDateInSeconds = endDate.toEpochSecond(ZoneOffset.UTC);
        long middleDatetimeInSeconds = (long)Math.ceil((endDateInSeconds - initDateInSeconds)/2.0) + initDateInSeconds;
        return LocalDateTime.ofEpochSecond(middleDatetimeInSeconds, 0, ZoneOffset.UTC);
    }
}
