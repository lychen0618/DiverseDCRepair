package com.lychen.mhsGenerationFamily.algorithm;

import com.lychen.mhsGenerationFamily.model.HyperGraph;

import java.util.ArrayList;
import java.util.List;

public class MMCSFactory {
    public static MMCSBase mmcsFactory(HyperGraph H, String enumMethod, String howToGetNextEdgeToCover,
                                       int certainNumber, List<Object> paras) {
        MMCSBase mmcsBase;
        if (enumMethod.contains("maxminTimes")){
            assert paras.size() >= 1 && paras.get(0) instanceof Integer;
            int totalNumberOfMhs = (((int) paras.get(0)) * certainNumber);
            List<Object> parasNew = new ArrayList<>();
            for(int i = 1; i < paras.size(); ++i) parasNew.add(paras.get(i));
            mmcsBase = generalFactory(H, enumMethod, totalNumberOfMhs, parasNew);
        }
        else mmcsBase = generalFactory(H, enumMethod, certainNumber, paras);
        mmcsBase.setHowToGetNextHyperEdgeToCover(howToGetNextEdgeToCover);
        return mmcsBase;
    }

    private static MMCSBase generalFactory(HyperGraph H, String enumMethod, int certainNumber, List<Object> paras) {
        MMCSBase mmcsBase = null;
        if (enumMethod.contains("basic")) mmcsBase = new MMCS(H);
        else if (enumMethod.contains("sub")) mmcsBase = subFactory(H, enumMethod, certainNumber, paras);
        else if (enumMethod.contains("random")) mmcsBase = randomFactory(H, enumMethod, certainNumber);
        else System.exit(-1);
        mmcsBase.setRequiredNumberOfMhs(certainNumber);
        return mmcsBase;
    }

    private static MMCSBase subFactory(HyperGraph H, String enumMethod, int certainNumber, List<Object> paras) {
        SubMMCS subMMCS;
        if (enumMethod.contains("autoInc")) {
            subMMCS = new SubMMCSAutoInc(H, certainNumber);
        } else subMMCS = new SubMMCS(H);
        subMMCS.setMinDistance((double) paras.get(0));
        subMMCS.setMaxGenerateMhsNumber((int) paras.get(1));
        return subMMCS;
    }

    private static MMCSBase randomFactory(HyperGraph H, String enumMethod, int certainNumber) {
        RandomGenerator randomGenerator;
        if (enumMethod.contains("autoInc")) {
            randomGenerator = new RandomGeneratorAutoInc(H, certainNumber);
        } else randomGenerator = new RandomGenerator(H);
        return randomGenerator;
    }
}
