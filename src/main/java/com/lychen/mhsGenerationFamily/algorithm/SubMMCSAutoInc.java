package com.lychen.mhsGenerationFamily.algorithm;

import com.lychen.mhsGenerationFamily.model.HyperGraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class SubMMCSAutoInc extends SubMMCS{
    public AutoInc autoInc;

    public SubMMCSAutoInc(HyperGraph H, int topK) {
        super(H);
        autoInc = new AutoInc(topK);
    }

    @Override
    public void changeFailFlag(){
        failedFlag = (numOfMhs == requiredNumberOfMhs ||
                generateMhsNumber == maxGenerateMhsNumber || autoInc.judge(numOfMhs, mhsSet));
    }

    @Override
    public List<BitSet> generateMHS() {
        subMMCSDfs(initialCand, new ArrayList<>());
        System.out.printf("MHS total/topK: %d/%d\n", mhsSet.size(), autoInc.topK);
        return autoInc.topKMhs;
    }
}
