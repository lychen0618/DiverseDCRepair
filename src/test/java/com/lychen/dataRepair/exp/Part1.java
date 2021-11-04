package com.lychen.dataRepair.exp;

import com.lychen.dataRepair.myDCRepair.EnumDCRepair;
import com.lychen.dataRepair.myDCRepair.detect.DetectManager;
import com.lychen.dataRepair.myDCRepair.enumerate.DiversityUtil;
import com.lychen.dataRepair.myDCRepair.enumerate.EnumManager;
import com.lychen.dataRepair.myDCRepair.io.InputManager;
import com.lychen.dataRepair.myDCRepair.model.RepairPair;
import com.lychen.mhsGenerationFamily.util.BitSetHelpFunc;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Part1 {
    @Test
    public void runRandomSingleRepair() throws IOException {
        String[] datasets = {"hospital"};
        String[][] tupleNums = {{"10000", "20000", "30000", "40000", "50000"}, {"10000"}};
        double[][] errorRates = {{2}, {1}};
        String repairAbility = "high";
        long startTime;
        for (int i = 0; i < datasets.length; ++i) {
            for (String tupleNum : tupleNums[i]) {
                for (double errorRate : errorRates[i]) {
                    for (int time = 1; time <= 5; ++time) {
                        System.out.println("\nTime: " + time + " Dataset: " + datasets[i] + " TupleNum: " +
                                tupleNum + " ErrorRate: " + errorRate);
                        startTime = System.currentTimeMillis();
                        randomSingleRepair(datasets[i], tupleNum, errorRate, repairAbility);
                        System.out.printf("total time cost: %dms\n", (System.currentTimeMillis() - startTime));
                    }
                }
            }
        }
    }

    /**
     * 使用randomMVC得到一个覆盖，然后根据这个覆盖修复
     */
    private void randomSingleRepair(String dataset, String tupleNum, double errorRate, String repairAbility)
            throws IOException {
        EnumDCRepair em = new EnumDCRepair();
        em.runInputAndDetect(dataset, tupleNum, errorRate, repairAbility, "");
        em.runEnum(1);
        List<RepairPair> singleRepair = em.runRepair().get(0);
        em.runEval(singleRepair);
    }

    /**
     * 比较subMVC和randomMVC得到的多样性覆盖的结果
     */
    @Test
    public void runCompareSubAndRandom() throws IOException {
        String[] datasets = {"hospital"};
        //"10000", "20000", "30000", "40000", "50000"
        //1, 2, 3, 4, 5
        String[][] tupleNums = {{"10000", "20000", "30000", "40000", "50000"}, {"10000"}};
        double[][] errorRates = {{2}, {1}};
        String repairAbility = "high";
        int topK = 100;
        for (int i = 0; i < datasets.length; ++i) {
            for (String tupleNum : tupleNums[i]) {
                for (double errorRate : errorRates[i]) {
                    System.out.println("\nDataset: " + datasets[i] + " TupleNum: " + tupleNum + " ErrorRate: " + errorRate);
                    EnumDCRepair enumDCRepair = new EnumDCRepair();
                    enumDCRepair.im = new InputManager(datasets[i], tupleNum, errorRate, repairAbility);
                    enumDCRepair.dm = new DetectManager(enumDCRepair.im);
                    enumDCRepair.dm.runDetectAndBuildHyperGraph(false);
                    enumDCRepair.em = new EnumManager(enumDCRepair.dm.H, enumDCRepair.im);
                    String howToGetNextEdgeToCover = "random";
                    long startTime;
                    //test sub
//                    List<Object> subPara1 = new ArrayList<Object>() {{
//                        add(0.1);
//                        add(Integer.MAX_VALUE);
//                    }};
//                    startTime = System.currentTimeMillis();
//                    DiversityUtil.testCoversResult(enumDCRepair.em.runMMCSEnumerate(
//                            "sub", howToGetNextEdgeToCover, topK, subPara1));
//                    System.out.println("sub-0.1 time cost: " + (System.currentTimeMillis() - startTime));
//                    List<Object> subPara2 = new ArrayList<Object>() {{
//                        add(0.01);
//                        add(100000);
//                    }};
//                    startTime = System.currentTimeMillis();
//                    DiversityUtil.testCoversResult(enumDCRepair.em.runMMCSEnumerate(
//                            "sub", howToGetNextEdgeToCover, topK, subPara2));
//                    System.out.println("sub-0.01 time cost: " + (System.currentTimeMillis() - startTime));
//                    List<Object> subPara3 = new ArrayList<Object>() {{
//                        add(0.001);
//                        add(10000);
//                    }};
//                    startTime = System.currentTimeMillis();
//                    DiversityUtil.testCoversResult(enumDCRepair.em.runMMCSEnumerate(
//                            "sub", howToGetNextEdgeToCover, topK, subPara3));
//                    System.out.println("sub-0.001 time cost: " + (System.currentTimeMillis() - startTime));
//                    List<Object> subPara4 = new ArrayList<Object>() {{
//                        add(0.0001);
//                        add(10000);
//                    }};
//                    startTime = System.currentTimeMillis();
//                    DiversityUtil.testCoversResult(enumDCRepair.em.runMMCSEnumerate(
//                            "sub", howToGetNextEdgeToCover, topK, subPara4));
//                    System.out.println("sub-0.0001 time cost: " + (System.currentTimeMillis() - startTime));
                    //test random [0, S-1] [0.5(S-1), S-1]
                    startTime = System.currentTimeMillis();
                    DiversityUtil.testCoversResult(enumDCRepair.em.runMMCSEnumerate(
                            "random", howToGetNextEdgeToCover, topK, null));
                    System.out.println("random time cost: " + (System.currentTimeMillis() - startTime));
                }
            }
        }
    }

    /**
     * 比较randomMVC和各种策略结合得到的多样性值
     */
    @Test
    public void runRandomMVCWithStrategy() throws IOException {
        String[] datasets = {"hospital"};
        String[][] tupleNums = {{"10000", "20000", "30000", "40000", "50000"}, {"10000"}};
        double[][] errorRates = {{2}, {1}};
        String repairAbility = "high";
        int topK = 100;
        for (int i = 0; i < datasets.length; ++i) {
            for (String tupleNum : tupleNums[i]) {
                for (double errorRate : errorRates[i]) {
                    System.out.println("\nDataset: " + datasets[i] + " TupleNum: " + tupleNum + " ErrorRate: " + errorRate);
                    EnumDCRepair enumDCRepair = new EnumDCRepair();
                    enumDCRepair.im = new InputManager(datasets[i], tupleNum, errorRate, repairAbility);
                    enumDCRepair.dm = new DetectManager(enumDCRepair.im);
                    enumDCRepair.dm.runDetectAndBuildHyperGraph(false);
                    enumDCRepair.em = new EnumManager(enumDCRepair.dm.H, enumDCRepair.im);
                    String howToGetNextEdgeToCover = "order";
                    long startTime;
                    startTime = System.currentTimeMillis();
//                    List<Object> para1 = new ArrayList<Object>() {{
//                        add(10);
//                    }};
//                    DiversityUtil.testCoversResult(enumDCRepair.em.runMMCSEnumerate(
//                            "random bestOfTimes", howToGetNextEdgeToCover, topK, para1));
//                    System.out.println("bestOfTimes time cost: " + (System.currentTimeMillis() - startTime));
//                    startTime = System.currentTimeMillis();
//                    List<Object> para2 = new ArrayList<Object>() {{
//                        add(10);
//                    }};
//                    DiversityUtil.testCoversResult(enumDCRepair.em.runMMCSEnumerate(
//                            "random maxminTimes", howToGetNextEdgeToCover, topK, para2));
//                    System.out.println("maxminTimes time cost: " + (System.currentTimeMillis() - startTime));
//                    startTime = System.currentTimeMillis();
                    DiversityUtil.testCoversResult(enumDCRepair.em.runMMCSEnumerate(
                            "random autoInc", howToGetNextEdgeToCover, topK, null));
                    System.out.println("autoInc time cost: " + (System.currentTimeMillis() - startTime));
                }
            }
        }
    }

    /**
     * 测试压缩超图后，randomMVC的运行情况，时间和多样性结果
     */
    @Test
    public void runRandomMVCOfCompressedGraph() throws IOException {
        String[] datasets = {"tax"};
        String[][] tupleNums = {{"20000"}, {"10000"}};
        //1, 2, 3, 4, 5
        double[][] errorRates = {{1, 2, 3, 4, 5}, {1}};
        String repairAbility = "high";
        int certainNumber = 20;
        for (int i = 0; i < datasets.length; ++i) {
            for (String tupleNum : tupleNums[i]) {
                for (double errorRate : errorRates[i]) {
                    System.out.println("\nDataset: " + datasets[i] + " TupleNum: " + tupleNum + " ErrorRate: " + errorRate);
                    EnumDCRepair enumDCRepair = new EnumDCRepair();
                    enumDCRepair.im = new InputManager(datasets[i], tupleNum, errorRate, repairAbility);
                    //不压缩超图
                    enumDCRepair.dm = new DetectManager(enumDCRepair.im);
                    enumDCRepair.dm.runDetectAndBuildHyperGraph(false);
                    enumDCRepair.em = new EnumManager(enumDCRepair.dm.H, enumDCRepair.im);
                    String howToGetNextEdgeToCover = "random";
                    long startTime;
                    List<Object> para1 = new ArrayList<Object>() {{
                        add(10);
                    }};
                    startTime = System.currentTimeMillis();
                    List<BitSet> covers1 = enumDCRepair.em.runMMCSEnumerate(
                            "random maxminTimes", howToGetNextEdgeToCover, certainNumber, para1);
                    System.out.println("not compress time cost: " + (System.currentTimeMillis() - startTime));
                    for (BitSet cover : covers1) System.out.print(cover.cardinality() + " ");
                    System.out.println();
                    DiversityUtil.testCoversResult(covers1);
                    //压缩超图
                    enumDCRepair.dm = new DetectManager(enumDCRepair.im);
                    enumDCRepair.dm.runDetectAndBuildHyperGraph(true);
                    enumDCRepair.em = new EnumManager(enumDCRepair.dm.H, enumDCRepair.im);
                    howToGetNextEdgeToCover = "random";
                    startTime = System.currentTimeMillis();
                    List<BitSet> covers2 = enumDCRepair.em.runMMCSEnumerate(
                            "random maxminTimes", howToGetNextEdgeToCover, certainNumber, para1);
                    System.out.println("compress time cost: " + (System.currentTimeMillis() - startTime));
                    for (BitSet cover : covers2) System.out.print(cover.cardinality() + " ");
                    System.out.println();
                    DiversityUtil.testCoversResult(covers2);
                }
            }
        }
    }

    /**
     * 测试共享计算的效果(randomMVC选不同的随机分布)
     */
    @Test
    public void runTestShareComputing() throws IOException {
        String[] datasets = {"hospital"};
        String[][] tupleNums = {{"10000", "20000", "30000", "40000", "50000"}, {"10000"}};
        double[][] errorRates = {{2}, {1}};
        String repairAbility = "high";
        int certainNumber = 100;
        for (int i = 0; i < datasets.length; ++i) {
            for (String tupleNum : tupleNums[i]) {
                for (double errorRate : errorRates[i]) {
                    System.out.println("\nDataset: " + datasets[i] + " TupleNum: " + tupleNum + " ErrorRate: " + errorRate);
                    EnumDCRepair em = new EnumDCRepair();
                    em.runInputAndDetect(datasets[i], tupleNum, errorRate, repairAbility, "");
                    em.setCompressGraph(true);
                    em.runEnum(certainNumber);
                    List<List<RepairPair>> repairResult = em.runRepair();
                    //for(List<RepairPair> one : repairResult) em.runEval(one);
                    List<List<RepairPair>> dfsRepairResult = em.runDfsRepair();
                    //for(List<RepairPair> one : dfsRepairResult) em.runEval(one);
                    //assert isEqualRepair(repairResult, dfsRepairResult);
                }
            }
        }
    }

    /**
     * randomMVC+maxminTimes+两个优化+repair和holistic对比
     */
    @Test
    public void compareThreeMethods() throws IOException {
        String[] datasets = {"tax"};
        String[][] tupleNums = {{"20000"}, {"10000"}};
        double[][] errorRates = {{1}, {1}};
        String repairAbility = "high";
        long startTime, endTime;
        int certainNumber = 100;
        for (int i = 0; i < datasets.length; ++i) {
            for (String tupleNum : tupleNums[i]) {
                for (double errorRate : errorRates[i]) {
                    System.out.println("\nDataset: " + datasets[i] + " TupleNum: " + tupleNum + " ErrorRate: " + errorRate);
//                    // holistic
//                    startTime = System.currentTimeMillis();
//                    HolisticRepair holisticRepair = new HolisticRepair();
//                    holisticRepair.run(datasets[i], tupleNum, errorRate, repairAbility);
//                    endTime = System.currentTimeMillis();
//                    System.out.printf("###Method: holistic Time: %fs RepairedCellNumber: %d\n",
//                            (endTime - startTime) * 1.0 / 1000, holisticRepair.repairCells.size());
                    // vioFree
//                    startTime = System.currentTimeMillis();
//                    VioFreeRepair vioFreeRepair = new VioFreeRepair();
//                    vioFreeRepair.run(datasets[i], tupleNum, errorRate, repairAbility);
//                    endTime = System.currentTimeMillis();
//                    System.out.printf("###Method: vioFree Time: %fs RepairedCellNumber: %d\n",
//                            (endTime - startTime) * 1.0 / 1000, vioFreeRepair.repairCells.size());
                    // our method
                    startTime = System.currentTimeMillis();
                    EnumDCRepair enumDCRepair = new EnumDCRepair();
                    enumDCRepair.setCompressGraph(true);
                    enumDCRepair.runInputAndDetect(datasets[i], tupleNum, errorRate, repairAbility, "");
                    enumDCRepair.em = new EnumManager(enumDCRepair.dm.H, enumDCRepair.im);
                    List<Object> para1 = new ArrayList<Object>() {{
                        add(1);
                    }};
                    List<BitSet> covers = enumDCRepair.em.runMMCSEnumerate(
                            "random autoInc", "random", certainNumber, null);
                    enumDCRepair.covers = BitSetHelpFunc.getIntSetCovers(covers);
                    List<List<RepairPair>> dfsRepairResult = enumDCRepair.runDfsRepair();
                    endTime = System.currentTimeMillis();
                    System.out.printf("###Method: ourMethod Time: %fs\n", (endTime - startTime) * 1.0 / 1000);
                    for (List<RepairPair> one : dfsRepairResult) enumDCRepair.runEval(one);

                }
            }
        }
    }

    private boolean isEqualRepair(List<List<RepairPair>> dfsRepair, List<List<RepairPair>> basicRepair) {
        boolean equal = true;
        if (basicRepair.size() != dfsRepair.size()) equal = false;
        else {
            for (int i = 0; i < basicRepair.size(); ++i) {
                List<RepairPair> oneRepair = basicRepair.get(i);
                List<RepairPair> oneDfsRepair = dfsRepair.get(i);
                if (oneRepair.size() != oneDfsRepair.size()) equal = false;
                else {
                    for (int j = 0; j < oneDfsRepair.size(); ++j) {
                        equal = oneDfsRepair.get(j).equals(oneRepair.get(j));
                        if (!equal) break;
                    }
                }
                if (!equal) break;
            }
        }
        return equal;
    }

    //比较dfsRepair和basicRepair
    @Test
    public void optimization() throws IOException {
        String[] datasets = {"tax"};
        String num = "20000";
        double errorRate = 1;
        String repairAbility = "high";
        long startTime, endTime;
        for (String dataset : datasets) {
            int certainNumber = 100;
            System.out.printf("###Dataset: %s\n", dataset);

            EnumDCRepair enumDCRepair = new EnumDCRepair();
            enumDCRepair.runInputAndDetect(dataset, num, errorRate, repairAbility, "");
            enumDCRepair.runEnum(certainNumber);
            startTime = System.currentTimeMillis();
            List<List<RepairPair>> dfsResult = enumDCRepair.runDfsRepair();
            endTime = System.currentTimeMillis();
            int durationOfDfsRepair = (int) (endTime - startTime);

            startTime = System.currentTimeMillis();
            List<List<RepairPair>> basicResult = enumDCRepair.runRepair();
            endTime = System.currentTimeMillis();
            int durationOfBasicRepair = (int) (endTime - startTime);
            System.out.println("dfsCount: " + enumDCRepair.dfsRepairCount + " basicCount: "
                    + enumDCRepair.basicRepairCount + " ratio" +
                    (enumDCRepair.basicRepairCount * 1.0 / enumDCRepair.dfsRepairCount));
            System.out.printf("Dfs repair: %dms Basic repair: %dms IsEqual: %b\n",
                    durationOfDfsRepair, durationOfBasicRepair, isEqualRepair(dfsResult, basicResult));
        }
    }
}
