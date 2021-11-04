package com.lychen.mhsGenerationFamily.algorithm;

import com.lychen.mhsGenerationFamily.model.HyperGraph;
import com.lychen.mhsGenerationFamily.model.IntSet;
import com.lychen.mhsGenerationFamily.util.MMCSUtil;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class MMCS extends MMCSBase {
    public MMCS(HyperGraph H) {
        super(H);
    }

    @Override
    public List<BitSet> generateMHS() {
        basicMMCSDfs(initialCand, new ArrayList<>());
        return mhsSet;
    }

    private void basicMMCSDfs(BitSet cand, List<IntSet> crit) {
        if (failedFlag) return;
        if (uncov.isEmpty()) {
            ++numOfMhs;
            mhsSet.add((BitSet) S.clone());
            changeFailFlag();
            return;
        }

        BitSet candCopy = (BitSet) cand.clone();
        IntSet nextCoverEdge = getGoodEdgeToCover(candCopy);
        IntSet.andNot(candCopy, nextCoverEdge);

        for (int vertex : nextCoverEdge.get()) {
            if (!MMCSUtil.vertex_would_violate(crit, H.getVertexHitting(vertex))) {
                try {
                    //List<IntSet> removed_criticals = new ArrayList<>();
                    //MMCSUtil.update_crit_and_uncov(removed_criticals, crit, uncov, T.getHyperEdge(vertex));
                    List<IntSet> critClone = new ArrayList<>();
                    for (IntSet intSet : crit) critClone.add(intSet.clone());
                    MMCSUtil.update_crit_and_uncov_new(critClone, uncov, H.getVertexHitting(vertex));
                    S.flip(vertex);
                    basicMMCSDfs(candCopy, critClone);
                    if (failedFlag) return;
                    S.flip(vertex);
                    candCopy.set(vertex);
                    //MMCSUtil.restore_crit_and_uncov(removed_criticals, crit, uncov);
                    MMCSUtil.restore_crit_and_uncov_new(critClone, uncov);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
