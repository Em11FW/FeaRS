package similarity;

import java.util.ArrayList;
import java.util.List;

public class GetIndexOfCorpus {
    public List<Integer> getIndexOfCorpus(long size){
        long sum = 0;
        sum = size*(size+1)/2;
        System.out.println(sum);
        List<Long> indexes = new ArrayList<>();
        List<Integer> realIndexes = new ArrayList<>();
        for(long i = 0; i < sum; i += sum/40){
            indexes.add(i);
        }
        sum = 0;
        int j = 0;
        for(int i = 1; i < size; i++){
            sum += (size-i);
            if(j >= indexes.size()){
                break;
            }
            if(indexes.get(j) < sum){
                realIndexes.add(i-1);
                j += 1;
            }
        }
        return realIndexes;
    }

}
