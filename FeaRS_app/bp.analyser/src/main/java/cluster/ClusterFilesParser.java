package cluster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ClusterFilesParser {
    private static ClusterFilesParser clusterFilesParserInstance = null;
    private ClusterFilesParser(){}

    public static ClusterFilesParser getInstance(){
        if(clusterFilesParserInstance == null){
            clusterFilesParserInstance = new ClusterFilesParser();
        }
        return clusterFilesParserInstance;
    }

    public HashMap<Long, Long> makeIDHashMap(String outputPath) throws IOException {
        String methodsMapPathname = Paths.get(outputPath,"log","methods-mapping.txt").toString();
        HashMap<Long, Long> map = new HashMap<>();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(methodsMapPathname));
        while ((line = reader.readLine()) != null){
            String[] parts = line.split(" ", 2);
            String value = parts[1];
            parts[1] = value.substring(1, value.indexOf("."));
            long key = Long.parseLong(parts[0]);
            long newValue = Long.parseLong(parts[1]);
            map.put(key, newValue);
        }
        return map;
    }

    public HashMap<Long, ArrayList<Long>> makeClusterHashMap(String outputPath, double clusterThreshold) throws IOException{
        String clusterThresholdStr = String.format("%.2f", clusterThreshold);
        String filePath = Paths.get(outputPath,"cluster","clusters-"+clusterThresholdStr+".txt").toString();
        HashMap<Long, ArrayList<Long>> map = new HashMap<>();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        while((line = reader.readLine()) != null){
            String[] parts = line.split("-", 2);
            String idArray = parts[0];
            ArrayList<Long> methodsIdArray = stringToIntArray(idArray);
            long clusterId = Long.parseLong(parts[1]);
            map.put(clusterId, methodsIdArray);
        }
        return map;
    }

    private ArrayList<Long> stringToIntArray(String str){
        String[] splits =  str.replace("[","").replace("]","").split(", ");
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(splits));
        ArrayList<Long> newArrayList = new ArrayList<>();

        for(String elem : arrayList){
            newArrayList.add(Long.parseLong(elem));
        }
        return newArrayList;
    }
}
