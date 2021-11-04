package com.lychen.dataRepair.myDCRepair;

import com.lychen.dataRepair.myDCRepair.detect.DetectManager;
import com.lychen.dataRepair.myDCRepair.enumerate.CoverTree;
import com.lychen.dataRepair.myDCRepair.enumerate.EnumManager;
import com.lychen.dataRepair.myDCRepair.enumerate.TreeNode;
import com.lychen.dataRepair.myDCRepair.io.InputManager;
import com.lychen.dataRepair.myDCRepair.model.Cell;
import com.lychen.dataRepair.myDCRepair.model.RepairPair;
import com.lychen.dataRepair.myDCRepair.model.Violation;
import com.lychen.dataRepair.myDCRepair.repair.RepairManager;
import com.lychen.dataRepair.myDCRepair.util.EvalRepairResult;
import com.lychen.mhsGenerationFamily.model.IntSet;
import com.lychen.mhsGenerationFamily.util.BitSetHelpFunc;

import java.io.IOException;
import java.util.*;

public class EnumDCRepair {
    //读数据集，规则
    public InputManager im;
    //检测冲突，生成冲突超图
    public DetectManager dm;
    //生成覆盖
    public EnumManager em;
    public List<IntSet> covers;
    //修复
    public RepairManager rm = new RepairManager();
    private boolean compressGraph = false;
    public int dfsRepairCount = 0, basicRepairCount = 0;

    public void setCompressGraph(boolean compressGraph) {
        this.compressGraph = compressGraph;
    }

    public void runInputAndDetect(String dataset, String num, Double errorRate,
                                  String repairAbility, String method) throws IOException {
        im = (method.equals("") ? new InputManager(dataset, num, errorRate, repairAbility) :
                new InputManager(dataset, num, method, errorRate));
        dm = new DetectManager(im);
        dm.runDetectAndBuildHyperGraph(compressGraph);
    }

    public void runEnum(int certainNumber) {
        em = new EnumManager(dm.H, im);
        String enumMethod = "random";
        String howToGetNextEdgeToCover = "order";
        List<BitSet> bitSetCovers = em.runMMCSEnumerate(enumMethod, howToGetNextEdgeToCover, certainNumber, null);
        covers = BitSetHelpFunc.getIntSetCovers(bitSetCovers);
        //TODO:把covers中每个cover的顶点按一定顺序排列
//        long startTime = System.currentTimeMillis();
//        arrangeCellOrder();
//        System.out.println("arrange time cost: " + (System.currentTimeMillis() - startTime));
//        System.out.println("number of mhs to repair: " + covers.size());
    }

    private void arrangeCellOrder() {
        Map<Integer, Integer> timeCount = new HashMap<>();
        for (IntSet cover : covers) {
            for (int vertex : cover.get()) {
                if (!timeCount.containsKey(vertex)) timeCount.put(vertex, 0);
                timeCount.put(vertex, timeCount.get(vertex) + 1);
            }
        }
        for (IntSet cover : covers) {
            cover.get().sort((o1, o2) -> timeCount.get(o2) - timeCount.get(o1));
        }
    }

    /**
     * 分别计算每个cover的修复
     */
    public List<List<RepairPair>> runRepair() {
        System.out.println("############### repair(single/one by one) ###############");
        long startTime = System.currentTimeMillis();
        List<List<RepairPair>> repairResult = new ArrayList<>();
        for (IntSet cover : covers) {
            repairResult.add(routineForRepairMhs(cover));
        }
        System.out.printf("repair finish in %fs\n", (System.currentTimeMillis() - startTime) * 1.0 / 1000);
        return repairResult;
    }

    private List<RepairPair> routineForRepairMhs(IntSet cover) {
        List<RepairPair> result = new ArrayList<>();
        Map<Cell, RepairPair> cellMapRepair = new HashMap<>();
        Map<Integer, Violation> violations = cloneViolationMap(dm.violationMap);
        Map<String, Map<String, List<Integer>>> clonedEquivalenceMap = cloneEquivalenceMap(im.equivalenceMap);
        //modifyCells: 覆盖中的cell
        List<Cell> modifyCells = new ArrayList<>();
        for (int cellIndex : cover.get()) {
            modifyCells.add(dm.cells.get(cellIndex));
        }
        //按顺序逐个修复cell
        for (Cell cell : modifyCells) {
            ++basicRepairCount;
            repairOneCell(cell, result, clonedEquivalenceMap);
            cellMapRepair.put(cell, result.get(result.size() - 1));
            //删除cell有关的超边
            for (int k : dm.cellListMap.get(cell)) {
                violations.remove(k);
            }
        }
        //检测是否消除了所有的冲突
//        if (im.config.isDebug()) {
//            Map<String, Map<String, List<Integer>>> oldEquivalenceMap = im.equivalenceMap;
//            im.rebuildEquivalenceMapAndHashTable();
//            DetectManager detectManager = new DetectManager(im);
//            detectManager.runDetectAndBuildHyperGraph(compressGraph);
//            im.equivalenceMap = oldEquivalenceMap;
//        }
        if (violations.size() != 0) {
            System.out.println("Repair failure.");
        }
        recoverTable(cellMapRepair);
        return result;
    }

    /**
     * dfs得到覆盖的修复
     */
    public List<List<RepairPair>> runDfsRepair() {
        if(im.config.isDebug()){
            for(IntSet cover : covers){
                List<Cell> modifyCells = new ArrayList<>();
                for (int cellIndex : cover.get()) {
                    modifyCells.add(dm.cells.get(cellIndex));
                }
                int common = 0;
                for(Cell cell : modifyCells){
                    if(im.noisyCells.containsKey(cell)) ++common;
                }
                System.out.println("repair " + modifyCells.size() + " error " + im.noisyCells.size() + " common " + common);
            }
        }
        System.out.println("############### repair(dfs) ###############");
        long startTime = System.currentTimeMillis();
        List<List<RepairPair>> repairResult = new ArrayList<>();
        CoverTree coverTree = new CoverTree(covers);
//        System.out.println(coverTree.getNumberOfTreeNode());
//        for (int i = 1; i < covers.size(); ++i) {
//            IntSet a = covers.get(i - 1);
//            IntSet b = covers.get(i);
//            int common = Math.min(a.size(), b.size());
//            for (int j = 0; j < a.size() && j < b.size(); ++j) {
//                if (a.get(j) != b.get(j)) {
//                    common = j;
//                    break;
//                }
//            }
//            System.out.printf("%d/%d/%d\n", a.size(), b.size(), common);
//        }
        List<RepairPair> tempRepair = new ArrayList<>();
        dfsRepair(coverTree.root, tempRepair, repairResult);
        System.out.printf("repair finish in %fs\n", (System.currentTimeMillis() - startTime) * 1.0 / 1000);
        return repairResult;
    }

    private void dfsRepair(TreeNode treeNode, List<RepairPair> tempRepair, List<List<RepairPair>> repairResult) {
        if (treeNode.children.size() == 0) {
            repairResult.add(new ArrayList<>(tempRepair));
            System.out.println("myRepair repairCellNumber: " + tempRepair.size());
            return;
        }
        for (int cellIndex : treeNode.children.keySet()) {
            Cell cell = dm.cells.get(cellIndex);
            repairOneCell(cell, tempRepair, im.equivalenceMap);
            ++dfsRepairCount;
            dfsRepair(treeNode.children.get(cellIndex), tempRepair, repairResult);
            recoverCellRepair(cell, tempRepair);
        }
    }

    //修复cell
    private void repairOneCell(Cell cell, List<RepairPair> tempRepair, Map<String, Map<String, List<Integer>>> equivalenceMap) {
        String repair = rm.getOneCellRepair(cell, equivalenceMap, im.rules, im.table);
        //更新equivalenceMap
        equivalenceMap.get(cell.getAttr()).get(cell.getValue()).remove(cell.getTid());
        if (!repair.equals("freshValue")) {
            if (!equivalenceMap.get(cell.getAttr()).containsKey(repair)) {
                System.out.println("There doesn't exist a repair value for this cell.Repair failure!");
            }
            equivalenceMap.get(cell.getAttr()).get(repair).add(cell.getTid());
        }
        String oldVal = cell.getValue();
        cell.setValue(repair);
        tempRepair.add(new RepairPair(cell, oldVal));
    }

    //修复效果指标计算
    public void runEval(List<RepairPair> oneRepair) {
        Map<Cell, String> repairCells = new HashMap<>();
        for (RepairPair repairPair : oneRepair) {
            int tid = Integer.parseInt(repairPair.cell.split("\\.")[0]);
            String attr = repairPair.cell.split("\\.")[1];
            repairCells.put(em.im.table.getCell(tid, attr), repairPair.newValue);
        }
        runEval(repairCells);
    }

    //修复效果指标计算
    public void runEval(Map<Cell, String> repairCells) {
        System.out.println("repair: " + repairCells.size() + " error: " + im.noisyCells.size());
        EvalRepairResult.getEvalResult(repairCells, im.noisyCells, "cell");
        EvalRepairResult.getEvalResult(repairCells, im.noisyCells, "val");
        EvalRepairResult.getEvalResult(repairCells, im.noisyCells, "cell-val");
    }

    //恢复表之前的状态
    private void recoverTable(Map<Cell, RepairPair> cellMapRepair) {
        for (Cell cell : cellMapRepair.keySet()) {
            cell.setValue(cellMapRepair.get(cell).oldValue);
        }
    }

    //恢复第k个cell在修复之前的状态
    private void recoverCellRepair(Cell cell, List<RepairPair> tempRepair) {
        RepairPair repairPair = tempRepair.get(tempRepair.size() - 1);
        assert !im.config.isDebug() || repairPair.cell.equals(cell.getTid() + "." + cell.getAttr());
        String oldVal = repairPair.oldValue;
        if (!repairPair.newValue.equals("freshValue")) {
            im.equivalenceMap.get(cell.getAttr()).get(cell.getValue()).remove(cell.getTid());
        }
        im.equivalenceMap.get(cell.getAttr()).get(oldVal).add(cell.getTid());
        cell.setValue(oldVal);
        tempRepair.remove(tempRepair.size() - 1);
    }

    //复制violationMap
    private Map<Integer, Violation> cloneViolationMap(Map<Integer, Violation> violationMap) {
        Map<Integer, Violation> res = new HashMap<>();
        for (int i : violationMap.keySet()) {
            res.put(i, violationMap.get(i).clone());
        }
        return res;
    }

    //复制equivalenceMap
    private Map<String, Map<String, List<Integer>>> cloneEquivalenceMap(Map<String, Map<String, List<Integer>>> equivalenceMap) {
        Map<String, Map<String, List<Integer>>> res = new HashMap<>();
        for (String attr : equivalenceMap.keySet()) {
            Map<String, List<Integer>> map = new HashMap<>();
            for (String value : equivalenceMap.get(attr).keySet()) {
                map.put(value, new ArrayList<>(equivalenceMap.get(attr).get(value)));
            }
            res.put(attr, map);
        }
        return res;
    }
}