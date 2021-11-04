package com.lychen.dataRepair.exp;

import com.lychen.dataRepair.myDCRepair.EnumDCRepair;
import com.lychen.dataRepair.myDCRepair.detect.DetectManager;
import com.lychen.dataRepair.myDCRepair.enumerate.EnumManager;
import com.lychen.dataRepair.myDCRepair.io.InputManager;
import org.junit.Test;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;

public class Part2 {
    @Test
    public void testCompressHyperGraph() throws IOException {
        String[] datasets = {"hospital"};
        String num = "40000";
        double errorRate = 1;
        String repairAbility = "high";
        for (String dataset : datasets) {
            System.out.printf("###Dataset: %s\n", dataset);

            EnumDCRepair enumDCRepairForGreedy = new EnumDCRepair();
            enumDCRepairForGreedy.im = new InputManager(dataset, num, errorRate, repairAbility);
            enumDCRepairForGreedy.dm = new DetectManager(enumDCRepairForGreedy.im);
            enumDCRepairForGreedy.dm.runDetectAndBuildHyperGraph(false);
            enumDCRepairForGreedy.em = new EnumManager(enumDCRepairForGreedy.dm.H, enumDCRepairForGreedy.im);
            System.out.println("greedy size: " + enumDCRepairForGreedy.em.getGreedyMhs().cardinality());
            List<BitSet> certainNumberMhsSetNotCompress = enumDCRepairForGreedy.em.runMMCSEnumerate
                    ("random", "order", 20, null);
            int maxSize = 0, minSize = Integer.MAX_VALUE;
            for (BitSet bitSet : certainNumberMhsSetNotCompress) {
                maxSize = Math.max(maxSize, bitSet.cardinality());
                minSize = Math.min(minSize, bitSet.cardinality());
            }
            System.out.printf("mhs of original hyperGraph max/min: %d/%d\n", maxSize, minSize);

            EnumDCRepair enumDCRepair = new EnumDCRepair();
            enumDCRepair.im = new InputManager(dataset, num, errorRate, repairAbility);
            enumDCRepair.dm = new DetectManager(enumDCRepair.im);
            enumDCRepair.dm.runDetectAndBuildHyperGraph(true);
            enumDCRepair.em = new EnumManager(enumDCRepair.dm.H, enumDCRepair.im);
            //TODO:测试在压缩后的超图上得出的覆盖是不是原超图的覆盖
            List<BitSet> certainNumberMhsSet = enumDCRepair.em.runMMCSEnumerate
                    ("random", "order", 20, null);
            maxSize = 0; minSize = Integer.MAX_VALUE;
            for (BitSet bitSet : certainNumberMhsSet) {
                maxSize = Math.max(maxSize, bitSet.cardinality());
                minSize = Math.min(minSize, bitSet.cardinality());
            }
            System.out.printf("mhs of compress hyperGraph max/min: %d/%d\n", maxSize, minSize);
        }
    }
}
