package com.lychen.dataRepair.myDCRepair.detect;

import com.lychen.dataRepair.myDCRepair.model.*;
import com.lychen.dataRepair.myDCRepair.io.InputManager;
import com.lychen.dataRepair.myDCRepair.util.Util;
import com.lychen.mhsGenerationFamily.model.HyperGraph;
import com.lychen.mhsGenerationFamily.model.IntSet;

import java.io.IOException;
import java.util.*;

public class DetectManager {
    public InputManager inputManager;
    public int vioNum = 0;
    /**
     * map vioID -> vio
     */
    public Map<Integer, Violation> violationMap;
    /**
     * map cell -> a set of relate violations
     */
    public Map<Cell, List<Integer>> cellListMap;
    /**
     * 冲突Cell在超图表示中的顶点序号，cells[0]表示0号顶点对应的冲突cell
     */
    public List<Cell> cells;
    /**
     * 冲突超图的超边数组
     */
    public List<IntSet> content;
    public HyperGraph H;
    public Map<Rule, List<Integer>> vioIndexRangeOfRule = new HashMap<>();

    public DetectManager(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void runDetectAndBuildHyperGraphAndWriteToFile(
            boolean compressHyperGraph, String fileRoot, String repairAbility) throws IOException {
        runDetectAndBuildHyperGraph(compressHyperGraph);
        Util.writeHypergraphToFile(
                H.getAllHyperEdges(), fileRoot, H.getNumOfVertex(), H.getNumOfHyperEdge(), repairAbility);
    }

    public void runDetectAndBuildHyperGraph(boolean compressHyperGraph) {
        System.out.println("############### detect ###############");
        long startTime = System.currentTimeMillis();
        vioNum = 0;
        violationMap = new HashMap<>();
        cellListMap = new HashMap<>();
        cells = new ArrayList<>();
        detectViolation(inputManager.rules, inputManager.singleRules, inputManager.table, inputManager.equivalenceMap);
        System.out.printf("detect finish in %dms\n", (System.currentTimeMillis() - startTime));
        System.out.println("############### build(compress) hypergraph ###############");
        startTime = System.currentTimeMillis();
        getHyperGraph(compressHyperGraph);
        H = new HyperGraph(cells.size(), content);
        H.getAllHyperEdges().sort(Comparator.comparingInt(IntSet::size));
        if (inputManager.config.isDebug()) {
            for (IntSet intSet : H.getAllHyperEdges()) {
                assert intSet.size() != 0;
            }
        }
        if (compressHyperGraph) {
            //H.compressHyperGraph();
            System.out.println("before compress vertex/edge: " + cellListMap.size() + "/" + violationMap.size());
            System.out.println("after compress vertex/edge: " + H.getNumOfVertex() + "/" + H.getNumOfHyperEdge());
        }
        System.out.printf("build(compress) hypergraph finish in %dms\n", (System.currentTimeMillis() - startTime));
    }

    //检测冲突
    private void detectViolation(List<Rule> rules, List<Integer> singleRules, Table table, Map<String, Map<String, List<Integer>>> equivalenceMap) {
        //检测单条元组的冲突(实验不考虑)
        if (singleRules.size() > 0) {
            for (int i = 0; i < table.getTupleSize(); ++i) {
                for (int ruleId : singleRules) {
                    if (isViolationSingle(i, rules.get(ruleId), table)) {
                        Violation violation = new Violation(ruleId, i, -1);
                        violationMap.put(vioNum, violation);
                        for (String attr : rules.get(ruleId).getAttrs()) {
                            Cell cell = table.getCell(i, attr);
                            updateCellListMap(cell);
                        }
                        vioNum++;
                    }
                }
            }
        }
        //检测元组对的冲突
        for (Rule rule : rules) {
            vioIndexRangeOfRule.put(rule, new ArrayList<>());
            vioIndexRangeOfRule.get(rule).add(violationMap.size());
            if (singleRules.contains(rule.getRuleId())) continue;

            List<List<Integer>> blockedTuples = new ArrayList<>();
            if (rule.containEQPredicate()) {
                blockedTuples = block(rule, table, equivalenceMap);
            } else {
                List<Integer> temp = new ArrayList<>();
                for (int i = 0; i < table.getTupleSize(); ++i) temp.add(i);
                blockedTuples.add(temp);
            }

            if (rule.onlyContainEQPredicate()) {
                for (List<Integer> tuples : blockedTuples) {
                    for (int ti = 0; ti < tuples.size(); ++ti) {
                        for (int tj = ti + 1; tj < tuples.size(); ++tj) {
                            Violation violation = new Violation(rule.getRuleId(), tuples.get(ti), tuples.get(tj));
                            addNewViolation(violation, rule, tuples.get(ti), tuples.get(tj), table);
                        }
                    }
                }
            } else {
                for (List<Integer> tuples : blockedTuples) {

                    List<Integer> hashcodeForTuple = inputManager.hashTableForARule.get(rule);
                    List<Integer> deduplicatedTuples = new ArrayList<>();
                    Map<Integer, List<Integer>> tupleGroup = new HashMap<>();
                    for (int tupleId : tuples) {
                        Integer hashcode = hashcodeForTuple.get(tupleId);
                        if (!tupleGroup.containsKey(hashcode)) {
                            tupleGroup.put(hashcode, new ArrayList<>());
                            deduplicatedTuples.add(tupleId);
                        }
                        tupleGroup.get(hashcode).add(tupleId);
                    }

                    for (int i = 0; i < deduplicatedTuples.size(); i++) {
                        for (int j = i + 1; j < deduplicatedTuples.size(); j++) {
                            int tupleI = deduplicatedTuples.get(i), tupleJ = deduplicatedTuples.get(j);
                            boolean ij = isViolation(tupleI, tupleJ, rule, table);
                            boolean ji = false;
                            if (!ij) ji = isViolation(tupleJ, tupleI, rule, table);
                            if (ij || ji) {
                                for (int tid1 : tupleGroup.get(hashcodeForTuple.get(tupleI))) {
                                    for (int tid2 : tupleGroup.get(hashcodeForTuple.get(tupleJ))) {
                                        Violation violation = ij ? new Violation(rule.getRuleId(), tid1, tid2) :
                                                new Violation(rule.getRuleId(), tid2, tid1);
                                        addNewViolation(violation, rule, tid1, tid2, table);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            vioIndexRangeOfRule.get(rule).add(violationMap.size());
            System.out.printf("%s: %d violations\n",
                    rule.toString(), vioIndexRangeOfRule.get(rule).get(1) - vioIndexRangeOfRule.get(rule).get(0));
        }

        System.out.println("The total violations/cells: " + violationMap.size() + "/" + cellListMap.keySet().size());
    }

    //单条元组是否是一个冲突
    private boolean isViolationSingle(int i, Rule rule, Table table) {
        for (Predicate predicate : rule.getAtoms()) {
            String leftVal, rightVal;
            leftVal = table.getCellValue(i, predicate.getLeftAttr());
            if (predicate.getIsSingle()) {
                rightVal = predicate.getRightAttr();
            } else rightVal = table.getCellValue(i, predicate.getRightAttr());
            if (!validTwoValue(leftVal, rightVal, predicate.getOp())) return false;
        }
        return true;
    }

    //一对元组是否是一个冲突
    private boolean isViolation(int i, int j, Rule rule, Table table) {
        for (Predicate predicate : rule.getAtoms()) {
            if (predicate.getOp().equals("=")) continue;
            if (!isValid(i, j, predicate, table)) return false;
        }
        return true;
    }

    //判断元组对<i,j>是否满足谓词predicate
    private boolean isValid(int i, int j, Predicate predicate, Table table) {
        String leftValue = table.getCellValue(i, predicate.getLeftAttr());
        String rightValue = table.getCellValue(j, predicate.getRightAttr());
        if (leftValue.equals("freshValue") || rightValue.equals("freshValue")) return false;
        return validTwoValue(leftValue, rightValue, predicate.getOp());
    }

    private boolean validTwoValue(String leftValue, String rightValue, String op) {
        boolean result = false;
        double leftDouble = 0;
        double rightDouble = 0;
        if (!op.equals("=") && !op.equals("!=")) {
            leftDouble = (leftValue.equals("NaN") ? Double.MAX_VALUE : Double.parseDouble(leftValue));
            rightDouble = (rightValue.equals("NaN") ? Double.MAX_VALUE : Double.parseDouble(rightValue));
        }
        switch (op) {
            case "=":
                result = leftValue.equals(rightValue);
                break;
            case "!=":
                result = !leftValue.equals(rightValue);
                break;
            case ">":
                result = leftDouble > rightDouble;
                break;
            case ">=":
                result = leftDouble >= rightDouble;
                break;
            case "<":
                result = leftDouble < rightDouble;
                break;
            case "<=":
                result = leftDouble <= rightDouble;
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 更新cellListMap
     */
    private void updateCellListMap(Cell cell) {
        if (cellListMap.containsKey(cell)) {
            cellListMap.get(cell).add(vioNum);
        } else {
            List<Integer> temp = new ArrayList<>();
            temp.add(vioNum);
            cellListMap.put(cell, temp);
        }
    }

    /**
     * 把新的Violation添加到violationMap
     */
    private void addNewViolation(Violation violation, Rule rule, int tid1, int tid2, Table table) {
        violationMap.put(vioNum, violation);
        for (String attr : rule.getAttrs()) {
            Cell leftCell = table.getCell(tid1, attr);
            //System.out.print(tuples.get(i) + "." + attr + " ");
            updateCellListMap(leftCell);
            Cell rightCell = table.getCell(tid2, attr);
            updateCellListMap(rightCell);
            //System.out.print(tuples.get(j) + "." + attr + " ");
        }
        //System.out.println();
        vioNum++;
    }

    private List<List<Integer>> block(Rule rule, Table table, Map<String, Map<String, List<Integer>>> equivalenceMap) {
        List<List<Integer>> result = new ArrayList<>();
        List<String> equalAttr = rule.getEQAttr();
        if (equalAttr.size() == 1) {
            Map<String, List<Integer>> commonValueGroup = equivalenceMap.get(equalAttr.get(0));
            for (String value : commonValueGroup.keySet()) {
                if (value.equals("freshValue")) continue;
                result.add(commonValueGroup.get(value));
            }
        } else if (equalAttr.size() == 2) {
            Map<Integer, List<Integer>> commonValueGroup = new HashMap<>();
            Map<String, Map<String, Integer>> map = new HashMap<>();
            int hashIndex = 0;
            for (int i = 0; i < table.getTupleSize(); ++i) {
                String val1 = table.getCellValue(i, equalAttr.get(0));
                String val2 = table.getCellValue(i, equalAttr.get(1));
                if (val1.equals("freshValue") || val2.equals("freshValue")) continue;
                int hashcode;
                if (!map.containsKey(val1)) {
                    hashcode = hashIndex++;
                    map.put(val1, new HashMap<String, Integer>() {{
                        put(val2, hashcode);
                    }});
                } else {
                    Map<String, Integer> map2 = map.get(val1);
                    if (!map2.containsKey(val2)) {
                        hashcode = hashIndex++;
                        map2.put(val2, hashcode);
                    } else {
                        hashcode = map2.get(val2);
                    }
                }
                if (!commonValueGroup.containsKey(hashcode)) {
                    commonValueGroup.put(hashcode, new ArrayList<>());
                }
                commonValueGroup.get(hashcode).add(i);
            }
            for (Integer value : commonValueGroup.keySet()) {
                result.add(commonValueGroup.get(value));
            }
        } else {
            System.exit(-1);
        }
        return result;
    }

    private List<List<Cell>> getViolationContainCells() {
        List<List<Cell>> violationContainCells = new ArrayList<>();
        for (int edgeIndex = 0; edgeIndex < violationMap.size(); ++edgeIndex) {
            violationContainCells.add(new ArrayList<>());
        }
        for (Cell cell : cellListMap.keySet()) {
            for (int edgeIndex : cellListMap.get(cell)) {
                violationContainCells.get(edgeIndex).add(cell);
            }
        }
        return violationContainCells;
    }

    /**
     * 建立冲突超图的超边数组List<BitSet>
     */
    private void getHyperGraph(boolean compressHyperGraph) {
        int edgeCnt = violationMap.size();
        Set<Cell> remainedCells;

        if (compressHyperGraph) {
            remainedCells = new HashSet<>();
            List<List<Cell>> violationContainCells = getViolationContainCells();
            for (Rule rule : inputManager.rules) {
                List<Integer> range = vioIndexRangeOfRule.get(rule);
//                if (rule.hasNumAttrs()) {
//                    int numCount = remainedCells.size();
//                    for (int curVioIndex = range.get(0); curVioIndex < range.get(1); ++curVioIndex) {
//                        remainedCells.addAll(violationContainCells.get(curVioIndex));
//                    }
//                    System.out.println("numAttrRule numCount: " + (remainedCells.size() - numCount));
//                    continue;
//                }
                if (range.get(1).equals(range.get(0))) continue;
                Set<Cell> involvedCell = new HashSet<>();
                for (int edgeIndex = range.get(0); edgeIndex < range.get(1); ++edgeIndex) {
                    involvedCell.addAll(violationContainCells.get(edgeIndex));
                }
                Map<Cell, List<Integer>> cellListMapForARule = new HashMap<>();
                for (Cell cell : involvedCell) {
                    List<Integer> temp = new ArrayList<>();
                    for (int vioIndex : cellListMap.get(cell)) {
                        if (vioIndex >= range.get(0) && vioIndex < range.get(1)) temp.add(vioIndex);
                    }
                    cellListMapForARule.put(cell, temp);
                }
                Set<Cell> generalCoverCell = new HashSet<>();
                Set<Integer> generalCoverVio = new HashSet<>();
                for (Cell cell : involvedCell) {
                    if (generalCoverCell.contains(cell)) continue;
                    Set<Cell> coveredCell = new HashSet<>();
                    Set<Integer> coveredVio = new HashSet<>();
                    helpFunc(cell, coveredCell, coveredVio, violationContainCells, cellListMapForARule);
                    generalCoverCell.addAll(coveredCell);
                    generalCoverVio.addAll(coveredVio);
                    //计算coveredCell中的cell出现在多少个冲突中
                    int minAppearance = Integer.MAX_VALUE, maxAppearance = 0;
                    for (Cell cellOfBlock : coveredCell) {
                        int appearance = cellListMapForARule.get(cellOfBlock).size();
                        minAppearance = Math.min(minAppearance, appearance);
                        maxAppearance = Math.max(maxAppearance, appearance);
                    }
                    if (minAppearance != maxAppearance) {
                        for (Cell cellOfBlock : coveredCell) {
                            if (cellListMapForARule.get(cellOfBlock).size() != minAppearance)
                                remainedCells.add(cellOfBlock);
                        }
                    } else {
                        remainedCells.addAll(coveredCell);
                    }
                }
                for (int curVioIndex = range.get(0); curVioIndex < range.get(1); ++curVioIndex) {
                    boolean notRemove = false;
                    for (Cell cell : violationContainCells.get(curVioIndex)) {
                        if (remainedCells.contains(cell)) {
                            notRemove = true;
                            break;
                        }
                    }
                    if (!notRemove) remainedCells.addAll(violationContainCells.get(curVioIndex));
                }
                if (inputManager.config.isDebug()) {
                    assert generalCoverVio.size() == (range.get(1) - range.get(0));
                    assert generalCoverCell.size() == involvedCell.size();
                }
            }
            //压缩超图第二阶段
            System.out.println("The first step of compress keeps " + remainedCells.size() + " vertices");
            Map<Integer, List<Cell>> cellVioCount = new HashMap<>();
            for (Cell cell : cellListMap.keySet()) {
                if (remainedCells.contains(cell)) continue;
                int vioCount = cellListMap.get(cell).size();
                if (!cellVioCount.containsKey(vioCount))
                    cellVioCount.put(vioCount, new ArrayList<>());
                cellVioCount.get(vioCount).add(cell);
            }
            int maxVioCount = 0;
            if (cellVioCount.size() > 1) {
                for (int i : cellVioCount.keySet()) maxVioCount = Math.max(maxVioCount, i);
                remainedCells.addAll(cellVioCount.get(maxVioCount));
                maxVioCount = cellVioCount.get(maxVioCount).size();
            }
            System.out.println("The second step of compress gets back " + maxVioCount + " vertices");

        } else remainedCells = cellListMap.keySet();

        content = new ArrayList<>(edgeCnt);
        for (int i = 0; i < edgeCnt; i++) content.add(new IntSet());
        //将cell按超边数量排序
        List<Map.Entry<Cell, List<Integer>>> cellList = new ArrayList<>(cellListMap.entrySet());
        cellList.sort((entry1, entry2) -> entry2.getValue().size() - entry1.getValue().size());
        for (Map.Entry<Cell, List<Integer>> entry : cellList) {
            if (!remainedCells.contains(entry.getKey())) continue;
            int cellIndex = cells.size();
            cells.add(entry.getKey());
            for (int vioIndex : entry.getValue()) {
                content.get(vioIndex).add(cellIndex);
            }
        }
    }

    private void helpFunc(Cell cell, Set<Cell> coveredCell, Set<Integer> coveredVio, List<List<Cell>> violationContainCells, Map<Cell, List<Integer>> cellListMapForARule) {
        for (int vioIndex : cellListMapForARule.get(cell)) {
            if (!coveredVio.contains(vioIndex)) {
                coveredVio.add(vioIndex);
                for (Cell newCell : violationContainCells.get(vioIndex)) {
                    if (!coveredCell.contains(newCell)) {
                        coveredCell.add(newCell);
                        helpFunc(newCell, coveredCell, coveredVio, violationContainCells, cellListMapForARule);
                    }
                }
            }
        }
    }
}
