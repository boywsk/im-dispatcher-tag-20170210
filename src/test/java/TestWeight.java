import java.util.TreeMap;

/**
 * Created by wangshikai on 17/1/3.
 */
public class TestWeight {

    private static int a =1,b=2,c=3,d=4;
    private static int[] weights = new int[]{a,b,c,d};
    private static TreeMap<Double,Integer> treeMap = new TreeMap<>();

    public static void caculWeight(double seed){
        double bingo = treeMap.tailMap(seed,false).firstKey();
        treeMap.put(bingo,treeMap.get(bingo)+1);
    }


    public static void main(String[] args) {
        //权重加权
        for(int w : weights){
            double weight = treeMap.size() == 0 ? 0:treeMap.lastKey().doubleValue();
            treeMap.put(w + weight,w);
        }

        for(int i = 0 ;i<100000;i++){
            double randomSeed = treeMap.lastKey()*Math.random();
            caculWeight(randomSeed);
        }
        System.out.println(treeMap);
    }
}
