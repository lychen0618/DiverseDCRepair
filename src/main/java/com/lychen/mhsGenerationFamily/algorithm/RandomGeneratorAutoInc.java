package com.lychen.mhsGenerationFamily.algorithm;

import com.lychen.mhsGenerationFamily.model.HyperGraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class RandomGeneratorAutoInc extends RandomGenerator {
    public AutoInc autoInc;

    public RandomGeneratorAutoInc(HyperGraph H, int topK) {
        super(H);
        isAutoInc = true;
        autoInc = new AutoInc(topK);
    }

    @Override
    public void changeFailFlag() {
        failedFlag = autoInc.judge(numOfMhs, mhsSet);
    }

    @Override
    public List<BitSet> generateMHS() {
//        while (true) {
//            commonInit();
//            SList = new ArrayList<>();
//            backStep = 0;
//            randomMMCS(initialCand, new ArrayList<>());
//            if(failedFlag) break;
//        }
        tempH = H;
        H = null;
        List<HyperGraph> blockedHyperGraph = tempH.getBlockedHyperGraph();
        coverIndex = 0;
        while(true){
            for (HyperGraph hyperGraph : blockedHyperGraph) {
                H = hyperGraph;
                commonInit();
                SList = new ArrayList<>();
                backStep = 0;
                failedFlag = false;
                randomMMCS(initialCand, new ArrayList<>());
            }
            ++coverIndex;
            assert coverIndex == mhsSet.size();
            failedFlag = false;
            changeFailFlag();
            if(failedFlag) break;
        }
        System.out.printf("MHS total/topK: %d/%d\n", mhsSet.size(), autoInc.topK);
        System.out.printf("basic/advance autoInc: %d/%d\n", autoInc.timeSum2, autoInc.timeSum1);
        return autoInc.topKMhs;
    }
}
