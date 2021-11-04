package com.lychen.dataRepair.exp;

import com.lychen.dataRepair.myDCRepair.EnumDCRepair;
import com.lychen.dataRepair.myDCRepair.detect.DetectManager;
import com.lychen.dataRepair.myDCRepair.enumerate.DiversityUtil;
import com.lychen.dataRepair.myDCRepair.enumerate.EnumManager;
import com.lychen.dataRepair.myDCRepair.io.InputManager;
import com.lychen.mhsGenerationFamily.model.HyperGraph;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class Part3 {
    @Test
    public void testDiversity() throws IOException {
        String[] datasets = {"tax"};
        String num = "20000";
        double errorRate = 1;
        String repairAbility = "high";
        int topK = 100;
        Set<String> type = new HashSet<String>() {
            {
                add("basic");
                add("sub");
                add("random");
                add("random bestOfTimes");
                add("random maxmin autoTimes");
                add("random maxmin autoInc");
            }
        };
        for (String dataset : datasets) {
            System.out.printf("###Dataset: %s\n", dataset);
            EnumDCRepair enumDCRepair = new EnumDCRepair();
            enumDCRepair.im = new InputManager(dataset, num, errorRate, repairAbility);
            enumDCRepair.dm = new DetectManager(enumDCRepair.im);
            enumDCRepair.dm.runDetectAndBuildHyperGraph(true);
            HyperGraph totalHyperGraph = null;
//            for (HyperGraph hyperGraph : enumDCRepair.dm.blockedHyperGraph) {
//                HyperGraph compressedGraph = HyperGraph.compressHyperGraph(hyperGraph);
//                if (totalHyperGraph == null) totalHyperGraph = compressedGraph;
//                else totalHyperGraph.addAnotherHyperEdges(compressedGraph.getAllHyperEdges());
//                System.out.printf("before compress v/e %d/%d%n",
//                        hyperGraph.getNumOfVertex(), hyperGraph.getNumOfHyperEdge());
//                System.out.printf("after compress v/e %d/%d%n",
//                        compressedGraph.getNumOfVertex(), compressedGraph.getNumOfHyperEdge());
//            }
//            assert totalHyperGraph != null;
//            System.out.printf("total graph v/e %d/%d%n",
//                    totalHyperGraph.getNumOfVertex(), totalHyperGraph.getNumOfHyperEdge());
//            enumDCRepair.em = new EnumManager(totalHyperGraph, enumDCRepair.im);
//            //都使用min来得到下一个要覆盖的超边
//            enumDCRepair.em.setHowToGetNextEdgeToCover("min");
//            //basic
//            if (type.contains("basic")) {
//                DiversityUtil.testCoversResult(enumDCRepair.em.basicMMCSEnumerate(topK));
//            }
//            //sub(不太行的通)
//            if (type.contains("sub")) {
//                enumDCRepair.em.setEnumMethod("sub");
//                List<Double> minDistance = new ArrayList<>();
//                minDistance.add(0.01);
//                DiversityUtil.testCoversResult(enumDCRepair.em.subMMCSEnumerate
//                        (topK, true, false, minDistance, 10000));
//            }
//            //random
//            if (type.contains("random")) {
//                enumDCRepair.em.setEnumMethod("random");
//                DiversityUtil.testCoversResult(enumDCRepair.em.randomOrRandomBiasedEnumerate(topK, false));
//            }
//            //random bestOfTimes
//            if (type.contains("random bestOfTimes")) {
//                enumDCRepair.em.setEnumMethod("random bestOfTimes");
//                DiversityUtil.testCoversResult(enumDCRepair.em.chooseBestFromTimesOfRandomTopK(topK, 10));
//            }
//            //random maxmin times
//            if (type.contains("random maxmin times")) {
//                enumDCRepair.em.setEnumMethod("random maxmin times");
//                List<Object> para = new ArrayList<>();
//                para.add(topK);para.add(10.0);para.add(false);
//                enumDCRepair.em.timesOrAutoTimesOrAutoIncOfMaxMinMethod(para);
//            }
//            //random maxmin autoTimes
//            if (type.contains("random maxmin autoTimes")) {
//                enumDCRepair.em.setEnumMethod("random maxmin autoTimes");
//                List<Object> para = new ArrayList<>();
//                para.add(topK);para.add(null);para.add(false);
//                enumDCRepair.em.timesOrAutoTimesOrAutoIncOfMaxMinMethod(para);
//            }
//            //random maxmin autoInc
//            if (type.contains("random maxmin autoInc")) {
//                enumDCRepair.em.setEnumMethod("random maxmin autoInc");
//                List<Object> para = new ArrayList<>();
//                para.add(topK);para.add(null);para.add(false);
//                enumDCRepair.em.timesOrAutoTimesOrAutoIncOfMaxMinMethod(para);
//            }
        }
    }

//    @Test
//    public void testRandom() throws IOException {
//        String[] datasets = {"tax"};
//        String num = "20000";
//        double errorRate = 1;
//        String repairAbility = "high";
//        int topK = 100;
//        boolean isCompress = true;
//        for (String dataset : datasets) {
//            System.out.printf("###Dataset: %s\n", dataset);
//            EnumDCRepair enumDCRepair = new EnumDCRepair();
//            enumDCRepair.im = new InputManager(dataset, num, errorRate, repairAbility);
//            enumDCRepair.dm = new DetectManager(enumDCRepair.im);
//            enumDCRepair.dm.runDetectAndBuildHyperGraph(isCompress);
//            if(!isCompress){
//                System.out.println("not compress");
//                enumDCRepair.em = new EnumManager(enumDCRepair.dm.H, enumDCRepair.im);
//                enumDCRepair.em.setHowToGetNextEdgeToCover("min");
//                long startTime = System.currentTimeMillis();
//                enumDCRepair.em.setEnumMethod("random");
//                DiversityUtil.testCoversResult(enumDCRepair.em.randomOrRandomBiasedEnumerate(topK, false));
//                System.out.printf("time cost: %dms\n", (System.currentTimeMillis() - startTime));
//            }
//            else{
//                System.out.println("compress");
//                HyperGraph totalHyperGraph = null;
//                for (HyperGraph hyperGraph : enumDCRepair.dm.blockedHyperGraph) {
//                    HyperGraph compressedGraph = HyperGraph.compressHyperGraph(hyperGraph);
//                    if (totalHyperGraph == null) totalHyperGraph = compressedGraph;
//                    else totalHyperGraph.addAnotherHyperEdges(compressedGraph.getAllHyperEdges());
//                    System.out.printf("before compress v/e %d/%d%n",
//                            hyperGraph.getNumOfVertex(), hyperGraph.getNumOfHyperEdge());
//                    System.out.printf("after compress v/e %d/%d%n",
//                            compressedGraph.getNumOfVertex(), compressedGraph.getNumOfHyperEdge());
//                }
//                assert totalHyperGraph != null;
//                System.out.printf("total graph v/e %d/%d%n",
//                        totalHyperGraph.getNumOfVertex(), totalHyperGraph.getNumOfHyperEdge());
//                enumDCRepair.em = new EnumManager(totalHyperGraph, enumDCRepair.im);
//                enumDCRepair.em.setHowToGetNextEdgeToCover("min");
//                long startTime = System.currentTimeMillis();
//                enumDCRepair.em.setEnumMethod("random");
//                DiversityUtil.testCoversResult(enumDCRepair.em.randomOrRandomBiasedEnumerate(topK, false));
//                System.out.printf("time cost: %dms\n", (System.currentTimeMillis() - startTime));
//            }
//        }
//    }
}
