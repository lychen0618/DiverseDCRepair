package com.lychen.mhsGenerationFamily.algorithm;

import com.lychen.mhsGenerationFamily.model.HyperGraph;
import com.lychen.mhsGenerationFamily.model.IntSet;
import com.lychen.mhsGenerationFamily.util.BitSetHelpFunc;
import com.lychen.mhsGenerationFamily.util.MMCSUtil;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class SubMMCS extends MMCSBase {
    // 前一个输出的覆盖
    private BitSet previousS = new BitSet();
    private double minDistance = -1;
    protected int generateMhsNumber = 0;
    protected int maxGenerateMhsNumber = Integer.MAX_VALUE;

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public void setMaxGenerateMhsNumber(int maxGenerateMhsNumber) {
        this.maxGenerateMhsNumber = maxGenerateMhsNumber;
    }

    public SubMMCS(HyperGraph H) {
        super(H);
    }

    @Override
    public List<BitSet> generateMHS() {
        subMMCSDfs(initialCand, new ArrayList<>());
        return mhsSet;
    }

    @Override
    void changeFailFlag() {
        failedFlag = (numOfMhs == requiredNumberOfMhs || generateMhsNumber == maxGenerateMhsNumber);
    }

    protected void subMMCSDfs(BitSet cand, List<IntSet> crit) {
        if (failedFlag) return;
        if (uncov.isEmpty()) {
            ++generateMhsNumber;
            changeFailFlag();
            if (checkSub()) return;
            BitSet sCopy = (BitSet) S.clone();
            previousS = sCopy;
            numOfMhs++;
            mhsSet.add(sCopy);
            changeFailFlag();
            return;
        }

        BitSet candCopy = (BitSet) cand.clone();
        IntSet nextCoverEdge = getGoodEdgeToCover(candCopy);
        IntSet.andNot(candCopy, nextCoverEdge);

        for (int vertex : nextCoverEdge.get()) {
            if (!MMCSUtil.vertex_would_violate(crit, H.getVertexHitting(vertex))) {
                try {
                    List<IntSet> critClone = new ArrayList<>();
                    for (IntSet intSet : crit) critClone.add(intSet.clone());
                    MMCSUtil.update_crit_and_uncov_new(critClone, uncov, H.getVertexHitting(vertex));
                    S.flip(vertex);
                    subMMCSDfs(candCopy, critClone);
                    if (failedFlag) return;
                    S.flip(vertex);
                    candCopy.set(vertex);
                    MMCSUtil.restore_crit_and_uncov_new(critClone, uncov);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 返回true，代表丢弃当前S
     */
    private boolean checkSub() {
        double distance;
        if (previousS.cardinality() != 0) {
            distance = BitSetHelpFunc.getDistance(previousS, S);
            return distance < minDistance;
        }
        return false;
    }
}
