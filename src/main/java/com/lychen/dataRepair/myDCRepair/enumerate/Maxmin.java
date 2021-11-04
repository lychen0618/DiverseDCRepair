package com.lychen.dataRepair.myDCRepair.enumerate;

import java.text.DecimalFormat;
import java.util.*;

public class Maxmin {
    private static double getInfoOfBitSetList(Set<Integer> res, List<BitSet> list, int[] disIndex, double[][] setPairDis, String str) {
        DecimalFormat df = new DecimalFormat("0.000");
        int numberOfBitSet = list.size();
        double avgDis = 0, minDis = 1, maxDis = 0;
        int sizeSum = 0;
        //first和second是两个距离最大的BitSet在list中的序号
        int first = 0, second = 0;
        for (int i = 0; i < numberOfBitSet; i++) {
            for (int j = i + 1; j < numberOfBitSet; j++) {
                double dis = setPairDis[disIndex[i]][disIndex[j]];
                avgDis += dis;
                minDis = Math.min(minDis, dis);
                if (dis > maxDis) {
                    maxDis = dis;
                    first = disIndex[i];
                    second = disIndex[j];
                }
            }
            sizeSum += list.get(i).cardinality();
        }
        avgDis /= (numberOfBitSet * 1.0 * (numberOfBitSet - 1) / 2);
        System.out.println(str + ": max/min/avg/avgSize: "
                + df.format(maxDis) + "/" + df.format(minDis) + "/"
                + df.format(avgDis) + "/" + df.format(sizeSum * 1.0 / list.size()));
        if (str.equals("BeforeMinmax")) {
            res.add(first);
            res.add(second);
        }
        return minDis;
    }

    private static double getTopKSetPair(List<BitSet> list, double[][] setPairs, List<BitSet> topKSetPair, int topK) {
        int[] disIndexBefore = new int[list.size()];
        for (int index = 0; index < list.size(); ++index) disIndexBefore[index] = index;
        Set<Integer> res = new HashSet<>();
        getInfoOfBitSetList(res, list, disIndexBefore, setPairs, "BeforeMinmax");
        while (res.size() < topK) {
            double maxDis = 0;
            int pos = 0;
            for (int i = 0; i < list.size(); i++) {
                if (res.contains(i)) continue;
                double minDis = 1;
                for (int j : res) {
                    minDis = Math.min(minDis, setPairs[i][j]);
                }
                if (minDis > maxDis) {
                    maxDis = minDis;
                    pos = i;
                }
            }
            res.add(pos);
        }
        int[] disIndexAfter = new int[topK];
        int curIndex = 0;
        for (int setIndex : res) {
            topKSetPair.add(list.get(setIndex));
            disIndexAfter[curIndex++] = setIndex;
        }
        return getInfoOfBitSetList(new HashSet<>(), topKSetPair, disIndexAfter, setPairs, "AfterMinmax");
    }

    public static double minmaxTopK(int topK, List<BitSet> list, List<BitSet> topKSetPair) {
        double[][] setPairs = new double[list.size()][list.size()];
        DiversityUtil.calculateSetPairs(list, setPairs);
        return getTopKSetPair(list, setPairs, topKSetPair, topK);
    }

    public static double minmaxTopK(int topK, List<BitSet> list, double[][] setPairs, List<BitSet> topKSetPair) {
        return getTopKSetPair(list, setPairs, topKSetPair, topK);
    }

    public static double minmaxTopK(List<BitSet> list, List<List<Double>> setDistance, int[] ans, double[] dis, int curSize) {
        DecimalFormat df = new DecimalFormat("0.000");
        Set<Integer> res = new HashSet<>();
        if (curSize == 0) {
            //找到距离最大的两个cover
            double maxDis = -1;
            int first = 0, second = 0;
            for (int i = 0; i < list.size(); ++i) {
                for (int j = i + 1; j < list.size(); ++j) {
                    if (setDistance.get(i).get(j) > maxDis) {
                        first = i;
                        second = j;
                        maxDis = setDistance.get(i).get(j);
                    }
                }
            }
            ans[0] = first;
            dis[0] = maxDis;
            ans[1] = second;
            dis[1] = maxDis;
            curSize = 2;
        }
        for (int i = 0; i < curSize; ++i) {
            res.add(ans[i]);
        }
        while (curSize < ans.length) {
            double maxdis = -1;
            int pos = 0;
            for (int i = 0; i < list.size(); i++) {
                if (res.contains(i)) continue;
                double mindis = 2;
                for (int j : res) {
                    mindis = Math.min(mindis, setDistance.get(i).get(j));
                }
                if (mindis > maxdis) {
                    maxdis = mindis;
                    pos = i;
                }
            }
            ans[curSize] = pos;
            dis[curSize] = maxdis;
            curSize++;
            res.add(pos);
        }

        double avgdiv = 0, mindiv = 2, maxdiv = -1;
        double num = 0;
        List<Integer> coverList = new ArrayList<>(res);
        for (int i = 0; i < coverList.size(); i++) {
            for (int j = i + 1; j < coverList.size(); j++) {
                num++;
                double distance = setDistance.get(coverList.get(i)).get(coverList.get(j));
                avgdiv += distance;
                mindiv = Math.min(mindiv, distance);
                maxdiv = Math.max(maxdiv, distance);
            }
        }
        avgdiv /= num;
        double sizediv = 0;
        for (int bs : ans) {
            sizediv += list.get(bs).cardinality();
        }
        sizediv /= res.size();

        System.out.println("minmax topK: max/min/avg/avgSize: " + df.format(maxdiv) + "/" + df.format(mindiv) + "/" + df.format(avgdiv) + "/" + df.format(sizediv));

        //测试
//        System.out.println("test if equal");
//        minmaxTopk(ans.length, list, ans);


        return mindiv;
    }
}
