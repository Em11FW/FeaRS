package analyser;

import analyser_db.PsqlDB;
import cluster.MethodsCluster;
import parser.XMLFileUtility;

public class Main {

    public static void main(String[] args){

        String clonedRepoLocation = args[0];
        String xmlFilesDestination = args[1];
        String methodStorePath = args[2];
        String clusterOutputPath = args[3];
        int numberOfThreads = Integer.parseInt(args[4]);
        double clusterThreshold = Double.parseDouble(args[5]);
        int methodsNumForClustering = Integer.parseInt(args[6]);
        String databaseUrl = args[7];
        String databaseName = args[8];
        String databaseUser = args[9];
        String databasePassword = args.length > 10? args[10] : "";

        PsqlDB.getInstance().connect(databaseUrl, databaseName,databaseUser,databasePassword);

        try {
            LogAnalyser analyser = new LogAnalyser(clonedRepoLocation, xmlFilesDestination);
            System.out.println("Analysis...");
            analyser.analyseChangeLog();
            System.out.println("Analysis ended.");
        } catch (Exception e)
        {
            System.err.println("ERROR: Something went wrong with analyzing repositories");
            e.printStackTrace();
        }

        int numberOfNonClusteredMethods = PsqlDB.getInstance().getNumberNonClusteredMethods();
        if(numberOfNonClusteredMethods >= methodsNumForClustering){
            System.out.println("Clustering...");
            MethodsCluster cluster = new MethodsCluster(methodStorePath, clusterOutputPath, numberOfThreads, clusterThreshold);
            cluster.createClusters();
            System.out.println("Clustering ended.");
        }
        else
        {
            System.out.println(" ** Clusters Skipped ** due to low number of new methods: "+ numberOfNonClusteredMethods+" < "+methodsNumForClustering);
        }
        PsqlDB.getInstance().deleteUnusedCommits();
        XMLFileUtility.getInstance().deleteXMLFilesDirectory(xmlFilesDestination);
        PsqlDB.getInstance().disconnect();
    }
}
