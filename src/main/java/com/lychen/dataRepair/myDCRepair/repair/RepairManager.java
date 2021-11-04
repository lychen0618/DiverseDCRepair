package com.lychen.dataRepair.myDCRepair.repair;

import com.lychen.dataRepair.myDCRepair.model.*;

import java.util.*;

public class RepairManager {
    //得到某个cell的修复
    public String getOneCellRepair(Cell cell, Map<String, Map<String, List<Integer>>> equivalenceMap, List<Rule> rules, Table table) {
        Fix fix = new Fix();
        for (Rule rule : rules) {
            //rule中不包含和cell相关的predicate，就跳过
            if (!rule.hasAttr(cell.getAttr())) continue;
            Predicate pred = new Predicate();
            BitSet leftSusp = new BitSet(table.getTupleSize());
            BitSet rightSusp = new BitSet(table.getTupleSize());
            //susp初始化，所有位全为1
            leftSusp.set(0, table.getTupleSize());
            rightSusp.set(0, table.getTupleSize());
            //这里只考虑了元组对，不考虑单个元组
            leftSusp.flip(cell.getTid());
            rightSusp.flip(cell.getTid());
            boolean onlyContainEQAndNEQ = true;
            for (Predicate predicate : rule.getAtoms()) {
                //实验中用到的每条规则中每个变量最多只出现在一个谓词中
                if (cell.getAttr().equals(predicate.getLeftAttr())) {
                    pred = predicate;
                    continue;
                }
                //left=true表示cell在左边
                BitSet bitSet = RepairManager.getSuspList(predicate, cell, equivalenceMap, table, true);
                leftSusp.and(bitSet);
                if (predicate.getOp().equals("=") || predicate.getOp().equals("!=")) {
                    rightSusp.and(bitSet);
                } else {
                    onlyContainEQAndNEQ = false;
                    //TODO:这里可以优化，不用重复计算
                    rightSusp.and(RepairManager.getSuspList(predicate, cell, equivalenceMap, table, false));
                }
            }
            Fix leftSuspFix = getBoundOfCell(pred, leftSusp, table, true);
            if (!leftSuspFix.hasRepair() || !fix.combineAnotherFix(leftSuspFix)) return "freshValue";
            if (!onlyContainEQAndNEQ) {
                Fix rightSuspFix = getBoundOfCell(pred, rightSusp, table, false);
                if (!rightSuspFix.hasRepair() || !fix.combineAnotherFix(rightSuspFix)) return "freshValue";
            }
        }
        return getRepairByBound(fix, cell, equivalenceMap);
    }

    //得到一个cell关于某个predicate的suspect
    public static BitSet getSuspList(Predicate predicate, Cell cell, Map<String, Map<String, List<Integer>>> equivalenceMap, Table table, boolean left) {
        // >  >=  <  <= 需要考虑元组对顺序问题
        int tableSize = table.getTupleSize();
        BitSet bitset = new BitSet(tableSize);
        String attr = predicate.getLeftAttr();
        String val = table.getCellValue(cell.getTid(), attr);
        String op = predicate.getOp();
        if (op.equals("=")) {
            List<Integer> list = equivalenceMap.get(attr).get(val);
            if (list != null) {
                for (int idx : list) bitset.set(idx);
            }
        } else if (op.equals("!=")) {
            List<Integer> list = equivalenceMap.get(attr).get(val);
            if (list != null) {
                for (int idx : list) bitset.set(idx);
            }
            bitset.flip(0, tableSize);
        } else {
            //TODO:这里好像可以不用排序。排序的复杂度更高。直接每个属性值和cell的值进行比较
            //将数值类属性排序
            List<String> attrList = orderByAttrValue(attr, equivalenceMap);
            int l = 0, r = attrList.size() - 1;
            while (l < r) {
                int mid = (l + r) / 2;
                if (Double.parseDouble(attrList.get(mid)) < Double.parseDouble(val)) {
                    l = mid + 1;
                } else r = mid;
            }
            int idx = l;
            if (op.equals("<") || op.equals("<=")) {
                if (left) {
                    //找value 大于cell.value的元组
                    if (op.equals("<") && attrList.get(idx).equals(val)) ++idx;
                    for (int i = idx; i < attrList.size(); i++) {
                        List<Integer> tupleList = equivalenceMap.get(attr).get(attrList.get(i));
                        if (tupleList == null) continue;
                        for (int k : tupleList) bitset.set(k);
                    }
                } else {
                    //找value 小于cell.value的元组
                    if (op.equals("<=") && attrList.get(idx).equals(val)) ++idx;
                    for (int i = 0; i < idx; i++) {
                        List<Integer> tupleList = equivalenceMap.get(attr).get(attrList.get(i));
                        if (tupleList == null) continue;
                        for (int k : tupleList) bitset.set(k);
                    }
                }
            } else {
                if (left) {
                    if (op.equals(">=") && attrList.get(idx).equals(val)) ++idx;
                    for (int i = 0; i < idx; i++) {
                        List<Integer> tupleList = equivalenceMap.get(attr).get(attrList.get(i));
                        if (tupleList == null) continue;
                        for (int k : tupleList) bitset.set(k);
                    }
                } else {
                    if (op.equals(">") && attrList.get(idx).equals(val)) ++idx;
                    for (int i = idx; i < attrList.size(); i++) {
                        List<Integer> tupleList = equivalenceMap.get(attr).get(attrList.get(i));
                        if (tupleList == null) continue;
                        for (int k : tupleList) bitset.set(k);
                    }
                }
            }
        }
        return bitset;
    }

    //根据每条规则得到的潜在冲突元组得到修复范围
    public Fix getBoundOfCell(Predicate predicate, BitSet susp, Table table, boolean left) {
        String attr = predicate.getLeftAttr();
        Fix fix = new Fix(predicate);
        if (susp.cardinality() == 0) return fix;
        String changedOp = left ? predicate.getReversedOp() : predicate.getRevertOp();
        for (int i = susp.nextSetBit(0); i != -1; i = susp.nextSetBit(i + 1)) {
            String value = table.getCellValue(i, attr);
            boolean hasRepair = value.equals("freshValue") || fix.changeBound(changedOp, value);
            if (!hasRepair) break;
        }
        return fix;
    }

    //根据修复范围得到cell的修复
    public String getRepairByBound(Fix fix, Cell cell, Map<String, Map<String, List<Integer>>> equivalenceMap) {
        String attr = cell.getAttr();
        int count = 0;
        String mostFrequentValue = "";
        //字符属性的修复
        if (fix.getIsStr()) {
            if (fix.getLowerBound().isEmpty()) {
                for (Map.Entry<String, List<Integer>> entry : equivalenceMap.get(attr).entrySet()) {
                    if (fix.isInBound(entry.getKey()) && entry.getValue().size() > count) {
                        count = entry.getValue().size();
                        mostFrequentValue = entry.getKey();
                    }
                }
            } else return fix.getLowerBound();
        }
        //数值属性的修复
        else {
            //TODO:找到fix范围内出现次数最多的值。看看怎样效率高。一个个检查，还是先排序再检查。
            for (Map.Entry<String, List<Integer>> entry : equivalenceMap.get(attr).entrySet()) {
                if (fix.isInBound(entry.getKey()) && entry.getValue().size() > count) {
                    count = entry.getValue().size();
                    mostFrequentValue = entry.getKey();
                }
            }
        }
        return (count == 0) ? "freshValue" : mostFrequentValue;
    }

    public static List<String> orderByAttrValue(String attr, Map<String, Map<String, List<Integer>>> equivalenceMap) {
        List<Double> list = new ArrayList<>();
        Map<Double, String> map = new HashMap<>();
        for (String val : equivalenceMap.get(attr).keySet()) {
            if (equivalenceMap.get(attr).get(val) != null) {
                double value = Double.parseDouble(val);
                list.add(value);
                map.put(value, val);
            }
        }
        Collections.sort(list);
        List<String> res = new ArrayList<>();
        for (Double d : list) {
            res.add(map.get(d));
        }
        return res;
    }
}
