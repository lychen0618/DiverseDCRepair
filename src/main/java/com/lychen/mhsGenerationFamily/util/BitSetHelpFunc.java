package com.lychen.mhsGenerationFamily.util;

import com.lychen.mhsGenerationFamily.model.IntSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BitSetHelpFunc {
    public static BitSet and(BitSet a, BitSet b) {
        BitSet re = new BitSet(a.size());
        int w = a.nextSetBit(0);
        while (w != -1) {
            if (b.get(w)) re.set(w);
            w = a.nextSetBit(w + 1);
        }
        return re;
    }

    public static BitSet or(BitSet a, BitSet b) {
        BitSet re = new BitSet(a.size());
        int w = a.nextSetBit(0);
        while (w != -1) {
            re.set(w);
            w = a.nextSetBit(w + 1);
        }
        w = b.nextSetBit(0);
        while (w != -1) {
            re.set(w);
            w = b.nextSetBit(w + 1);
        }
        return re;
    }

    public static BitSet sub(BitSet a, BitSet b) {
        BitSet re = new BitSet(a.size());
        int w = a.nextSetBit(0);
        while (w != -1) {
            if (!b.get(w)) re.set(w);
            w = a.nextSetBit(w + 1);
        }
        return re;
    }

    public static boolean equal(BitSet a, BitSet b){
        if(a.cardinality() != b.cardinality()) return false;
        for(int i : a.stream().toArray()){
            if(!b.get(i)) return false;
        }
        return true;
    }

    public static boolean isSubsetOf(BitSet a, BitSet b) {
        if (a.cardinality() > b.cardinality()) return false;
        int w = a.nextSetBit(0);
        while (w != -1) {
            if (!b.get(w)) return false;
            w = a.nextSetBit(w + 1);
        }
        return true;
    }

    public static List<IntSet> getIntSetCovers(List<BitSet> covers){
        List<IntSet> res = new ArrayList<>();
        for(BitSet cover : covers){
            res.add(new IntSet(cover));
        }
        return res;
    }

    //使用jaccard相似性计算距离
    public static double getDistance(BitSet preS, BitSet S) {
        int cnt = 0;
        for (int i = preS.nextSetBit(0); i != -1; i = preS.nextSetBit(i + 1)) {
            if (S.get(i)) cnt++;
        }
        return 1 - (double) cnt / (preS.cardinality() + S.cardinality() - cnt);
    }
}
