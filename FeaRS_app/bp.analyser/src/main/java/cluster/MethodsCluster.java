package cluster;

import analyser_db.PsqlDB;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static main.Cluster.getClusterFromMethods;

public class MethodsCluster {
    private String methodsPath;
    private String outputPath;
    private int numOfThreads;
    private double clusterThreshold;

    public MethodsCluster(String methodsPath, String outputPath, int numOfThreads, double clusterThreshold){
        this.methodsPath = methodsPath;
        this.outputPath = outputPath;
        this.numOfThreads = numOfThreads;
        this.clusterThreshold = clusterThreshold;
    }

    public void createClusters(){
        int nMethods = FilesUtility.getInstance().createMethodsFiles(methodsPath);
        System.out.format("\tClustering %d methods ...\n", nMethods);
        if(nMethods == 0) {
            return;
        }
        /**
         *      Let's say have a method id=3644 in database. We create "/temp/methods/3644.txt".
         *      Then we use Fengcai Clustering library. The problem is that this library has its own index.
         *      ** methodIdMap: Maps back from Fengcai's index to our database IDs
         *      ** clusterMethodsHM Output of clustering (based on Fengcai's index)
         */
        HashMap<Long, Long> methodIdMap;
        HashMap<Long, ArrayList<Long>> clusterMethodsHM;

        try{getClusterFromMethods(methodsPath, outputPath, numOfThreads, clusterThreshold);
            clusterMethodsHM = ClusterFilesParser.getInstance().makeClusterHashMap(outputPath, clusterThreshold);
            methodIdMap = ClusterFilesParser.getInstance().makeIDHashMap(outputPath);
        }catch (IOException e){
            System.err.println("Cannot create clusters");
            e.printStackTrace();
            return;
        }
        PsqlDB.getInstance().cleanClusterTable();
        saveClusters(methodIdMap, clusterMethodsHM);

        FilesUtility.getInstance().deleteFolder(methodsPath);
        FilesUtility.getInstance().deleteFolder(outputPath);
    }

    private void saveClusters(HashMap<Long, Long> methodIdMap, HashMap<Long, ArrayList<Long>> clusterMethodsHM) {
        System.out.println("Storing clusters in the database ... "+clusterMethodsHM.size());
        for(long key : clusterMethodsHM.keySet()){
            ImmutablePair<String, String> m = PsqlDB.getInstance().getMethodBody(methodIdMap.get(key));

            String centroidBody = m.left;
            String source = m.right;
            PsqlDB.getInstance().insertCluster(key,centroidBody, source);
            for(long id : clusterMethodsHM.get(key)){
                PsqlDB.getInstance().updateMethodCluster(methodIdMap.get(id),key);
            }
        }
        System.out.println("Storing clusters in the database [DONE} ");
    }
}
