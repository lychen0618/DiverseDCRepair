package com.lychen.dataRepair.myDCRepair.util;

import com.lychen.dataRepair.myDCRepair.model.Cell;

import java.util.Map;

public class EvalRepairResult {
    public static void getEvalResult(Map<Cell, String> repair, Map<Cell, String> error, String type) {
        double tp;
        if (type.equals("cell")) tp = getMetricValueOfCell(repair, error);
        else if (type.equals("val")) tp = getMetricValueOfVal(repair, error);
        else tp = getMetricValueOfCellAndVal(repair, error);
        System.out.print(type + "||");
        calculate(tp, repair, error);
    }

    public static double getMetricValueOfCell(Map<Cell, String> repair, Map<Cell, String> error) {
        double tp = 0;
        for (Map.Entry<Cell, String> repairPair : repair.entrySet()) {
            if (error.containsKey(repairPair.getKey())) {
                tp++;
            }
        }
        return tp;
    }

    public static double getMetricValueOfVal(Map<Cell, String> repair, Map<Cell, String> error) {
        double tp = 0;
        for (Map.Entry<Cell, String> repairPair : repair.entrySet()) {
            if (error.containsKey(repairPair.getKey())) {
                if (repairPair.getValue().equals(error.get(repairPair.getKey()))) {
                    tp++;
                }
            }
        }
        return tp;
    }

    public static double getMetricValueOfCellAndVal(Map<Cell, String> repair, Map<Cell, String> error) {
        double tp = 0;
        for (Map.Entry<Cell, String> repairPair : repair.entrySet()) {
            if (error.containsKey(repairPair.getKey())) {
                tp += 0.5;
                if (repairPair.getValue().equals(error.get(repairPair.getKey()))) {
                    tp += 0.5;
                }
            }
        }
        return tp;
    }

    private static void calculate(double tp, Map<Cell, String> repair, Map<Cell, String> error) {
        double precision = tp / repair.size();
        double recall = tp / error.size();
        double fMeasure = (tp == 0 ? 0 : 2 * precision * recall / (precision + recall));
        System.out.printf("cell change number: %d precision: %.2f recall: %.2f F-measure: %.2f\n", repair.size(), precision, recall, fMeasure);
    }
}
