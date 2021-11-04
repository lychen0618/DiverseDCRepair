package com.lychen.mhsGenerationFamily.algorithm;

import com.lychen.dataRepair.myDCRepair.enumerate.Maxmin;
import com.lychen.mhsGenerationFamily.util.BitSetHelpFunc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class AutoInc {
    public int maxNumberOfMhsForDiversity = 2000;
    public int topK;
    List<List<Double>> setDistance = new ArrayList<>();
    public List<BitSet> topKMhs = new ArrayList<>();
    int[] ans;
    int[] tempAns;
    double[] dis;
    double[] tempDis;
    int curSize = 0;
    int validateSuccessTime = 0;
    public int maxValidateSuccessTime = 5;
    public long timeSum1 = 0, timeSum2 = 0;

    public AutoInc(int topK) {
        this.topK = topK;
        ans = new int[topK];
        dis = new double[topK];
        tempAns = new int[topK];
        tempDis = new double[topK];
    }

    public boolean judge(int newMhsIndex, List<BitSet> mhsSet) {
        boolean failFlag = false;
        if (newMhsIndex == topK) {
            for (int i = 0; i < topK; ++i) {
                List<Double> temp = new ArrayList<>();
                for (int j = 0; j < topK; ++j) {
                    temp.add(0.0);
                }
                setDistance.add(temp);
            }
            for (int i = 0; i < topK; ++i) {
                for (int j = i + 1; j < topK; ++j) {
                    double distance = BitSetHelpFunc.getDistance(mhsSet.get(i), mhsSet.get(j));
                    setDistance.get(i).set(j, distance);
                    setDistance.get(j).set(i, distance);
                }
            }
            long startTime = System.currentTimeMillis();
            double minDis = Maxmin.minmaxTopK(mhsSet, setDistance, ans, dis, curSize);
            curSize = topK;
            timeSum1 += (System.currentTimeMillis() - startTime);
            //System.out.println("minDis: " + minDis);
        } else if (newMhsIndex > topK) {
            BitSet newCover = mhsSet.get(newMhsIndex - 1);
            int failedPos = validate(newCover, mhsSet, setDistance, ans, dis, curSize);

            long startTime = System.currentTimeMillis();
            int[] newTempAns = new int[topK];
            Maxmin.minmaxTopK(mhsSet, setDistance, newTempAns, tempDis, 0);
            for (int i = 0; i < topK; ++i) {
                if (tempAns[i] != newTempAns[i]) break;
            }
            tempAns = newTempAns;
            timeSum2 += (System.currentTimeMillis() - startTime);

            startTime = System.currentTimeMillis();
            //验证成功
            if (failedPos == curSize) {
                if (curSize < topK) {
                    //System.out.println("success maxmin");
                    double newMinDis = Maxmin.minmaxTopK(mhsSet, setDistance, ans, dis, curSize);
                    curSize = topK;
                    //System.out.println("minDis: " + newMinDis);
                    validateSuccessTime = 0;
                } else {
                    ++validateSuccessTime;
                }
            }
            //验证失败
            else {
                validateSuccessTime = 0;
                if (failedPos == 0) {
                    //System.out.println("failure maxmin");
                    double newMinDis = Maxmin.minmaxTopK(mhsSet, setDistance, ans, dis, failedPos);
                    curSize = topK;
                    //System.out.println("minDis: " + newMinDis);
                } else {
                    curSize = failedPos;
                }
            }
            if ((failFlag = !judgeCondition(validateSuccessTime, newMhsIndex))) {
                if (curSize < topK) {
                    double newMinDis = Maxmin.minmaxTopK(mhsSet, setDistance, ans, dis, curSize);
                    //System.out.println("minDis: " + newMinDis);
                }
                for (int i : ans) {
                    topKMhs.add(mhsSet.get(i));
                }
                //System.out.println("The total number of mhs: " + newMhsIndex + " topKMhs size: " + topKMhs.size());
            }
            timeSum1 += (System.currentTimeMillis() - startTime);
        }
        return failFlag;
    }

    private boolean judgeCondition(int validateSuccessTime, int numberOfMhs) {
        return validateSuccessTime <= maxValidateSuccessTime && numberOfMhs < maxNumberOfMhsForDiversity;
    }

    private int validate(BitSet newCover, List<BitSet> allGeneratedMhs, List<List<Double>> setDistance, int[] ans, double[] dis, int curSize) {
        //计算距离
        int newMhsIndex = allGeneratedMhs.size() - 1;
        setDistance.add(new ArrayList<>());
        double maxDis = -1;
        for (int i = 0; i < newMhsIndex; ++i) {
            double distance = BitSetHelpFunc.getDistance(allGeneratedMhs.get(i), newCover);
            setDistance.get(newMhsIndex).add(i, distance);
            setDistance.get(i).add(distance);
            maxDis = Math.max(maxDis, distance);
        }
        setDistance.get(newMhsIndex).add(0.0);
        long startTime = System.currentTimeMillis();
        //判断
        int failedPos = 0;
        if (maxDis <= dis[0]) {
            maxDis = Math.min(setDistance.get(ans[0]).get(newMhsIndex), setDistance.get(ans[1]).get(newMhsIndex));
            failedPos = 2;
            while (failedPos < curSize) {
                if (dis[failedPos] < maxDis) {
                    break;
                }
                maxDis = Math.min(maxDis, setDistance.get(ans[failedPos]).get(newMhsIndex));
                ++failedPos;
            }
        }
        timeSum1 += (System.currentTimeMillis() - startTime);
        return failedPos;
    }
}
