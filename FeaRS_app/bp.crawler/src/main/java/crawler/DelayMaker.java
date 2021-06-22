package crawler;

import java.util.Random;

public class DelayMaker {
    private static Random random = new Random();

    public static void threadWait(long time_ms){
        try{
//            System.out.println("Waiting.."+time);
            Thread.sleep(time_ms);
        }catch(InterruptedException e){
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static void threadWaitRandom(){
        threadWait(random.nextInt(2000));
    }

}
