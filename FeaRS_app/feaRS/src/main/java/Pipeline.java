import analyser_db.PsqlDB;
import crawler.Main;
import crawler.RepositoriesCrawler;
import org.quartz.*;

import java.time.LocalDateTime;

@DisallowConcurrentExecution
public class Pipeline implements Job {

    @Override
    public void execute(JobExecutionContext context){
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String dburl = dataMap.getString("dburl");
        String dbname = dataMap.getString("dbname");
        String dbuser = dataMap.getString("dbuser");
        String password = dataMap.getString("password");
        String repositoriesDestinationDir = dataMap.getString("repositoriesDestinationDir");
        String xmlFilesTempDestinationDir = dataMap.getString("xmlFilesTempDestinationDir");
        String methodsFilesDestinationDir = dataMap.getString("methodsFilesDestinationDir");
        String clusteringOutputFilesDestinationDir = dataMap.getString("clusteringOutputFilesDestinationDir");
        String numberOfClusterThreads = dataMap.getString("numberOfClusterThreads");
        String clusterThreshold = dataMap.getString("clusterThreshold");
        String newMethodsClusteringThreshold = dataMap.getString("newMethodsClusteringThreshold");
        String associationRuleSupport = dataMap.getString("associationRuleSupport");
        String associationRuleConfidence = dataMap.getString("associationRuleConfidence");
        String associationRuleMaxLength = dataMap.getString("associationRuleMaxLength");

        System.out.println("\n\n\n===========================================================");
        System.out.println("============================ PIPELINE =====================");

        try {
            //Crawler
            System.out.println("\n=======  PIPELINE 1/4: Crawler ");
            String[] crawlerArgs = {dburl, dbname, dbuser, password};
            crawler.Main.main(crawlerArgs);
            //Cloner
            System.out.println("\n=======  PIPELINE 2/4: Cloner ");
            String[] clonerArgs = {repositoriesDestinationDir, dburl, dbname, dbuser, password};
            cloner.Main.main(clonerArgs);
            //Analyser
            System.out.println("\n=======  PIPELINE 3/4: Analyser ");
            String[] analyserArgs = {repositoriesDestinationDir, xmlFilesTempDestinationDir, methodsFilesDestinationDir,
                    clusteringOutputFilesDestinationDir, numberOfClusterThreads, clusterThreshold,
                    newMethodsClusteringThreshold, dburl, dbname, dbuser, password};
            analyser.Main.main(analyserArgs);
            //ARule
            System.out.println("\n=======  PIPELINE 4/4: ARule ");
            String[] aruleArgs = {associationRuleSupport, associationRuleConfidence, associationRuleMaxLength,
                    dburl, dbname, dbuser, password};
            arule.ARuleMain.main(aruleArgs);
        } catch (Exception e)
        {
            System.err.println(" =*+*+*+*+*+* During the pipeline somewhere an error happened: ");
            e.printStackTrace();
        }
        System.out.println(" -------------- Job ended at "+ LocalDateTime.now()+" -----------");
    }

}
