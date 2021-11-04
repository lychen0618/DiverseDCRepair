package com.lychen.dataRepair.myDCRepair.enumerate;

import com.lychen.mhsGenerationFamily.algorithm.*;
import com.lychen.dataRepair.myDCRepair.io.InputManager;
import com.lychen.mhsGenerationFamily.model.HyperGraph;
import com.lychen.mhsGenerationFamily.model.IntSet;

import java.util.*;

public class EnumManager {
    private final HyperGraph H;
    public InputManager im;

    public EnumManager(HyperGraph H, InputManager im) {
        this.H = H;
        this.im = im;
    }

    //basic sub random
    //onetime bestOfTimes maxminTimes autoInc
    public List<BitSet> runMMCSEnumerate(String enumMethod, String howToGetNextEdgeToCover,
                                         int certainNumber, List<Object> paras) {
        System.out.println("############### enum(mmcs) ###############");
        long startTime = System.currentTimeMillis();
        List<BitSet> res = new ArrayList<>();
        if (enumMethod.contains("bestOfTimes")) {
            assert paras.size() == 1 && paras.get(0) instanceof Integer;
            int times = (int) paras.get(0);
            double diversityValue = 0;
            while (times-- > 0) {
                List<BitSet> tempAns = runMMCSEnumerate("random", howToGetNextEdgeToCover,
                        certainNumber, null);
                double tempDiversityValue = DiversityUtil.testCoversResult(tempAns);
                if (tempDiversityValue > diversityValue) {
                    diversityValue = tempDiversityValue;
                    res = tempAns;
                    System.out.println("choose this random generate topK mhs");
                }
            }
        } else {
            MMCSBase mmcsBase = MMCSFactory.mmcsFactory(H, enumMethod, howToGetNextEdgeToCover, certainNumber, paras);
            res = mmcsBase.generateMHS();
        }
        if (enumMethod.contains("maxminTimes")) {
            List<BitSet> tempTopK = new ArrayList<>();
            long st = System.currentTimeMillis();
            Maxmin.minmaxTopK(certainNumber, res, tempTopK);
            System.out.println("time of maxmin: ms" + (System.currentTimeMillis() - st));
            assert tempTopK.size() == certainNumber;
            System.out.printf("MHS total/topK: %d/%d\n", res.size(), certainNumber);
            res = tempTopK;
        }
        if (im.config.isDebug()) isMhs(res);
        System.out.printf("enum finish in %dms\n", (System.currentTimeMillis() - startTime));
        return res;
    }

    public BitSet getGreedyMhs() {
        System.out.println("############### enum(greedy) ###############");
        long startTime = System.currentTimeMillis();
        BitSet res = GreedyGenerator.findGreedyCover(H);
        if(im.config.isDebug()) isCover(res);
        System.out.printf("enum finish in %dms\n", (System.currentTimeMillis() - startTime));
        return res;
    }

    private void isMhs(List<BitSet> mhsSet) {
        for (BitSet bitSet : mhsSet) {
            assert isCover(bitSet);
            for (int w = bitSet.nextSetBit(0); w != -1; w = bitSet.nextSetBit(w + 1)) {
                BitSet newBitSet = (BitSet) bitSet.clone();
                newBitSet.flip(w);
                assert !isCover(newBitSet);
            }
        }
    }

    private boolean isCover(BitSet bitSet) {
        for (IntSet edge : H.getAllHyperEdges()) {
            if (!edge.intersects(bitSet)) return false;
        }
        return true;
    }
}
