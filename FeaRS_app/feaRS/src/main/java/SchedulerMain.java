import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerMain {

    public static void main(String[] args){
        String repositoriesDestinationDir = args[0];
        String xmlFilesTempDestinationDir = args[1];
        String methodsFilesDestinationDir = args[2];
        char lastchar = args[3].charAt(args[3].length()-1);
        String  clusteringOutputFilesDestinationDir = (lastchar=='/')? args[3] : args[3].concat("/");
        String numberOfClusterThreads = args[4];
        String clusterThreshold = args[5];
        String newMethodsClusteringThreshold = args[6];
        String associationRuleSupport = args[7];
        String associationRuleConfidence = args[8];
        String associationRuleMaxLength = args[9];
        String dburl = args[10];
        String dbname = args[11];
        String dbuser = args[12];
        String password = (args.length > 13) ? args[13] : "";
        System.out.println("Initializing Pipeline Scheduler ....");
        try {
             Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

             JobDetail pipelineJob = JobBuilder.newJob(Pipeline.class)
                    .withIdentity(Pipeline.class.getName())
                    .usingJobData("repositoriesDestinationDir", repositoriesDestinationDir)
                    .usingJobData("xmlFilesTempDestinationDir", xmlFilesTempDestinationDir)
                    .usingJobData("methodsFilesDestinationDir", methodsFilesDestinationDir)
                    .usingJobData("clusteringOutputFilesDestinationDir", clusteringOutputFilesDestinationDir)
                    .usingJobData("numberOfClusterThreads", numberOfClusterThreads)
                    .usingJobData("clusterThreshold", clusterThreshold)
                    .usingJobData("newMethodsClusteringThreshold", newMethodsClusteringThreshold)
                    .usingJobData("associationRuleSupport", associationRuleSupport)
                    .usingJobData("associationRuleConfidence", associationRuleConfidence)
                    .usingJobData("associationRuleMaxLength", associationRuleMaxLength)
                    .usingJobData("dburl", dburl)
                    .usingJobData("dbname", dbname)
                    .usingJobData("dbuser", dbuser)
                    .usingJobData("password", password)
                    .build();
             Trigger jobTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("SimpleTrigger", "Pipeline")
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(10) // interval between runs
                            .repeatForever()
                            .withMisfireHandlingInstructionIgnoreMisfires()) // if a scheduled job misfires it is ignored and process will execute to next scheduled time
                    .build();
             scheduler.scheduleJob(pipelineJob, jobTrigger);
            System.out.println("Starting Pipeline Scheduler (for the first time) ...");
             scheduler.start();
        }catch(SchedulerException e){
            System.err.println("Cannot run scheduled pipeline");
            e.printStackTrace();
        }
    }


}
