package com.lychen.dataRepair.myDCRepair.enumerate;

import com.lychen.mhsGenerationFamily.util.BitSetHelpFunc;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.List;

public class DiversityUtil {
    //输出覆盖间的多样性结果
    public static double testCoversResult(List<BitSet> covers) {
        DecimalFormat df = new DecimalFormat("0.000");
        double avg = 0, min = 1, max = 0, cnt = 0, size = 0;
        for (int i = 0; i < covers.size(); i++) {
            for (int j = i + 1; j < covers.size(); j++) {
                if (i == j) continue;
                double dis = BitSetHelpFunc.getDistance(covers.get(i), covers.get(j));
                cnt++;
                avg += dis;
                min = Math.min(min, dis);
                max = Math.max(max, dis);
            }
            size += covers.get(i).cardinality();
        }
        size /= covers.size();
        avg /= cnt;
        System.out.println("max/min/avg/avgSize/num: " + df.format(max) + "/" + df.format(min) +
                "/" + df.format(avg) + "/" + df.format(size) + "/" + covers.size());
        return min;
    }

    public static void calculateSetPairs(List<BitSet> list, double[][] setPairs) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                double dis = BitSetHelpFunc.getDistance(list.get(i), list.get(j));
                setPairs[i][j] = dis;
                setPairs[j][i] = dis;
            }
        }
    }
}
