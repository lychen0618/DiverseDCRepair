package com.lychen.mhsGenerationFamily.algorithm;

import com.lychen.mhsGenerationFamily.model.HyperGraph;
import com.lychen.mhsGenerationFamily.model.IntSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public abstract class MMCSBase {
    HyperGraph H;
    List<BitSet> mhsSet = new ArrayList<>();
    int numOfMhs = 0;
    int requiredNumberOfMhs = Integer.MAX_VALUE;
    boolean failedFlag = false;
    BitSet S;
    BitSet uncov;
    BitSet initialCand;
    //max min order random
    String howToGetNextHyperEdgeToCover = "order";
    Random random = new Random();
    int nextEdge;

    void commonInit(){
        S = new BitSet();
        uncov = new BitSet(H.getNumOfHyperEdge());
        uncov.flip(0, H.getNumOfHyperEdge());
        initialCand = (BitSet) H.getVertices().clone();
    }

    public MMCSBase(HyperGraph H) {
        this.H = H;
        commonInit();
    }

    public void setRequiredNumberOfMhs(int requiredNumberOfMhs) {
        this.requiredNumberOfMhs = requiredNumberOfMhs;
    }

    public void setHowToGetNextHyperEdgeToCover(String howToGetNextHyperEdgeToCover) {
        this.howToGetNextHyperEdgeToCover = howToGetNextHyperEdgeToCover;
    }

    public int getNumOfMhs() {
        return numOfMhs;
    }

    public abstract List<BitSet> generateMHS();

    void changeFailFlag() {
        failedFlag = (numOfMhs == requiredNumberOfMhs);
    }

    IntSet getGoodEdgeToCover(BitSet candCopy) {
        if (howToGetNextHyperEdgeToCover.equals("order")){
            nextEdge = uncov.nextSetBit(0);
            return IntSet.and(H.getHyperEdge(nextEdge), candCopy);
        }
        else if (howToGetNextHyperEdgeToCover.equals("random")) {
            int[] uncovEdges = uncov.stream().toArray();
            nextEdge = uncovEdges[random.nextInt(uncovEdges.length)];
            return IntSet.and(H.getHyperEdge(nextEdge), candCopy);
        } else {
            if(S.cardinality() == 0){
                int[] uncovEdges = uncov.stream().toArray();
                nextEdge = uncovEdges[random.nextInt(uncovEdges.length)];
                return IntSet.and(H.getHyperEdge(nextEdge), candCopy);
            }
            int i = uncov.nextSetBit(0);
            nextEdge = i;
            IntSet nextHyperEdgeToCover = IntSet.and(H.getHyperEdge(i), candCopy);
            for (int w = uncov.nextSetBit(i + 1); w != -1; w = uncov.nextSetBit(w + 1)) {
                IntSet temp = IntSet.and(H.getHyperEdge(w), candCopy);
                if (howToGetNextHyperEdgeToCover.equals("min")) {
                    if (temp.size() < nextHyperEdgeToCover.size()) {
                        nextEdge = w;
                        nextHyperEdgeToCover = temp;
                    }
                } else {
                    if (temp.size() > nextHyperEdgeToCover.size()) {
                        nextHyperEdgeToCover = temp;
                    }
                }
            }
            return nextHyperEdgeToCover;
        }
    }
}
