package com.lychen.mhsGenerationFamily.util;

import com.lychen.mhsGenerationFamily.model.IntSet;

import java.util.BitSet;
import java.util.List;

public class MMCSUtil {

    //判断要加入的顶点是否会破坏最小性
    public static boolean vertex_would_violate(List<IntSet> crit, IntSet vertex_hitting) {
        for (IntSet intSet : crit) {
            if (intSet.isSubsetOf(vertex_hitting)) return true;
        }
        return false;
    }

    //更新crit和uncov
    public static void update_crit_and_uncov(List<IntSet> removed_criticals, List<IntSet> crit, BitSet uncov, IntSet v_hittings) {
        for (IntSet intSet : crit) {
            removed_criticals.add(IntSet.and(intSet, v_hittings));
            intSet.andNot(v_hittings);
        }
        crit.add(IntSet.and(v_hittings, uncov));
        IntSet.andNot(uncov, v_hittings);
    }

    public static void update_crit_and_uncov_new(List<IntSet> crit, BitSet uncov, IntSet v_hittings) {
        for (IntSet intSet : crit) {
            intSet.andNot(v_hittings);
        }
        crit.add(IntSet.and(v_hittings, uncov));
        IntSet.andNot(uncov, v_hittings);
    }

    //恢复crit和uncov
    public static void restore_crit_and_uncov(List<IntSet> removed_criticals, List<IntSet> crit, BitSet uncov) {
        IntSet.or(uncov, crit.get(crit.size() - 1));
        crit.remove(crit.size() - 1);
        for (int i = 0; i < crit.size(); ++i) {
            crit.get(i).or(removed_criticals.get(i));
        }
    }

    public static void restore_crit_and_uncov_new(List<IntSet> crit, BitSet uncov) {
        IntSet.or(uncov, crit.get(crit.size() - 1));
    }
}
