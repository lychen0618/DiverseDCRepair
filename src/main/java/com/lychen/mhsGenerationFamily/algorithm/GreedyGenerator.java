package com.lychen.mhsGenerationFamily.algorithm;

import com.lychen.mhsGenerationFamily.model.HyperGraph;
import com.lychen.mhsGenerationFamily.model.IntSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class GreedyGenerator {
    public static BitSet findGreedyCover(HyperGraph H) {
        BitSet greedyCover = new BitSet(H.getNumOfVertex());
        BitSet uncoveredEdges = new BitSet(H.getNumOfHyperEdge());
        uncoveredEdges.flip(0, H.getNumOfHyperEdge());
        Random random = new Random();
        while (!uncoveredEdges.isEmpty()) {
            int max = 0;
            List<Integer> vertexList = new ArrayList<>();
            //long startTime = System.currentTimeMillis();
            for (int i = H.getVertices().nextSetBit(0); i != -1; i = H.getVertices().nextSetBit(i + 1)) {
                //TODO:这里可以提升效率吗？
                if (greedyCover.get(i)) continue;
                //if (T.getHyperEdges().get(i).cardinality() <= max) continue;
                IntSet edge = IntSet.and(H.getVertexHitting(i), uncoveredEdges);
                if (edge.size() > max) {
                    max = edge.size();
                    vertexList = new ArrayList<>();
                    vertexList.add(i);
                } else if (edge.size() == max) vertexList.add(i);
            }
//            System.out.println("chose vertexIndex: " + vertexIndex + " max: " + max);
//            long endTime = System.currentTimeMillis();
//            System.out.printf("loop duration: %dms\n", (endTime - startTime));
            int vertexIndex = vertexList.get(random.nextInt(vertexList.size()));
            greedyCover.set(vertexIndex);
            IntSet.andNot(uncoveredEdges, H.getVertexHitting(vertexIndex));
        }
        return greedyCover;
    }
}
