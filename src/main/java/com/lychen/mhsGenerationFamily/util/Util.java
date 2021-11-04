package com.lychen.mhsGenerationFamily.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Util {
    public static void randomGenerate(int numberOfVertex, int numberOfHyperEdge, int expectSize, String fileRoot) throws IOException {
        List<Integer> hyperEdgeSizeList = new ArrayList<>();
        int maxAppearance = Math.max(expectSize, numberOfVertex - expectSize + 1);
        System.out.println("maxAppearance: " + maxAppearance);
        int tempAppearance = maxAppearance;
        for (int pos = expectSize; pos >= 1; --pos, --tempAppearance) {
            for (int i = 0; i < tempAppearance; ++i) {
                hyperEdgeSizeList.add(pos);
            }
            if (pos == 1) System.out.println("size 1: " + tempAppearance);
        }
        tempAppearance = maxAppearance - 1;
        for (int pos = expectSize + 1; pos <= numberOfVertex; ++pos, --tempAppearance) {
            for (int i = 0; i < tempAppearance; ++i) {
                hyperEdgeSizeList.add(pos);
            }
            if (pos == numberOfVertex) System.out.printf("size %d: %d\n", pos, tempAppearance);
        }

        Random random = new Random();
        random.setSeed(1);

        String filePath = String.format("%s/random_v%d_e%d_a%d_%s.txt", fileRoot, numberOfVertex,
                numberOfHyperEdge, expectSize, UUID.randomUUID().toString().replaceAll("-", ""));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));

        for (int edgeIndex = 0; edgeIndex < numberOfHyperEdge; ++edgeIndex) {
            BitSet newEdge = new BitSet(numberOfVertex);
            int sizeOfNewEdge = hyperEdgeSizeList.get(random.nextInt(hyperEdgeSizeList.size()));
            int curSize = 0;
            while (curSize < sizeOfNewEdge) {
                int vertexIndex = random.nextInt(numberOfVertex);
                if (newEdge.get(vertexIndex)) continue;
                newEdge.set(vertexIndex);
                curSize++;
            }
            boolean first = true;
            for (int w = newEdge.nextSetBit(0); w != -1; w = newEdge.nextSetBit(w + 1)) {
                if (first) {
                    bufferedWriter.write(String.valueOf(w));
                    first = false;
                } else bufferedWriter.write(" " + w);
            }
            bufferedWriter.write("\n");
        }

        bufferedWriter.close();
    }

    private static List<List<Integer>> transformToIntList(List<BitSet> mhsSet) {
        List<List<Integer>> res = new ArrayList<>();
        for (BitSet mhs : mhsSet) {
            List<Integer> temp = new ArrayList<>();
            for (int w = mhs.nextSetBit(0); w != -1; w = mhs.nextSetBit(w + 1)) temp.add(w);
            res.add(temp);
        }
        res.sort((o1, o2) -> {
            if (o1.size() != o2.size()) return o1.size() - o2.size();
            for (int i = 0; i < o1.size(); ++i) {
                if (!o1.get(i).equals(o2.get(i))) return o1.get(i) - o2.get(i);
            }
            return 0;
        });
        return res;
    }

    public static boolean testEqual(List<BitSet> sets1, List<BitSet> sets2) {
        if (sets1.size() != sets2.size()) return false;
        List<List<Integer>> arr1 = transformToIntList(sets1);
        List<List<Integer>> arr2 = transformToIntList(sets2);
        for (int i = 0; i < arr1.size(); ++i) {
            if (arr1.get(i).size() != arr2.get(i).size()) return false;
            for (int j = 0; j < arr1.get(i).size(); ++j) {
                if (!arr1.get(i).get(j).equals(arr2.get(i).get(j))) return false;
            }
        }
        return true;
    }
}
