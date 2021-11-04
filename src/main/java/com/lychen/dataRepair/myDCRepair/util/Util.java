package com.lychen.dataRepair.myDCRepair.util;

import com.lychen.dataRepair.myDCRepair.model.Cell;
import com.lychen.dataRepair.myDCRepair.model.Table;
import com.lychen.mhsGenerationFamily.model.IntSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Util {

    //添加噪声
    public static Map<Cell, String> addNoise(String method, double rate, Table table) {
        if (method.equals("cell")) return addNoiseByCell(rate, table);
        else return addNoiseByTuple(rate, table);
    }

    public static Map<Cell, String> addNoiseByCell(double rate, Table table) {
        Map<Cell, String> res = new HashMap<>();
        Random random = new Random();
        int row = table.getTupleSize(), col = table.getAttrsSize();
        int total = row * col;
        double nums = rate * total;
        while (nums-- > 0) {
            int k = random.nextInt(total);
            int xpos = k / col;
            int ypos = k % col;
            Cell cell = table.getCell(xpos, table.getAttrs()[(ypos - 1 + col) % col]);
            if (res.containsKey(cell)) continue;
            String val = table.getCellValue(xpos, table.getAttrs()[(ypos - 1 + col) % col]);
            res.put(cell, val);
            String newVal = new String();
            if (table.getAttrType()[(ypos - 1 + col) % col]) {
                //属性为字符串，随机选取一个字母置为'X'
                char[] str = val.toCharArray();
                str[random.nextInt(val.length())] = 'X';
                newVal = String.valueOf(str);
            } else {
                //属性为数字
                newVal = String.valueOf(random.nextInt(3000));
            }
            cell.setValue(newVal);
            updateEquivalenceMap(table.getEquivalenceMap(), cell, val);
        }
        return res;
    }

    public static Map<Cell, String> addNoiseByTuple(double rate, Table table) {
        Random rand = new Random();
        Map<Cell, String> res = new HashMap<>();
        double nums = 0.03 * table.getTupleSize();
        while (nums-- > 0) {
            int row = rand.nextInt(table.getTupleSize());
            //每条tuple中随机修改1-2个cell
            int cnt = rand.nextInt(2) + 1;
//            int cnt=2;
            while (cnt > 0) {
                int idx = rand.nextInt(table.getAttrsSize());
                Cell cell = table.getCell(row, table.getAttrs()[idx]);
                if (res.containsKey(cell)) continue;
                String val = table.getCellValue(row, table.getAttrs()[idx]);
                res.put(cell, val);
                String newVal = new String();
                if (table.getAttrType()[idx]) {
                    //属性为字符串，随机选取一个字母置为'X'
                    char[] str = val.toCharArray();
                    str[rand.nextInt(val.length())] = 'X';
                    newVal = String.valueOf(str);
                } else {
                    //属性为数字
                    newVal = String.valueOf(rand.nextInt(3000));
                }
                cell.setValue(newVal);
                updateEquivalenceMap(table.getEquivalenceMap(), cell, val);
                cnt--;
            }
        }
        return res;
    }

    public static void updateEquivalenceMap(Map<String, Map<String, List<Integer>>> equivalenceMap, Cell cell, String oldValue) {
        Map<String, List<Integer>> attrMap = equivalenceMap.get(cell.getAttr());
        List<Integer> list = attrMap.get(oldValue);
        list.remove(cell.getTid());
        if (list.size() == 0) attrMap.remove(oldValue);
        if (attrMap.containsKey(cell.getValue())) {
            attrMap.get(cell.getValue()).add(cell.getTid());
        } else {
            List<Integer> temp = new ArrayList<>();
            temp.add(cell.getTid());
            attrMap.put(cell.getValue(), temp);
        }
    }

    public static void writeToEnumFile(List<BitSet> printedAlready, String fileName, String num, boolean isPart) throws IOException {
        String outFileName;
        if (isPart)
            outFileName = "./data/intermediate/mhs/part/" + fileName + "/" + fileName + "_" + num + "_cover.csv";
        else outFileName = "./data/intermediate/mhs/all/" + fileName + "/" + fileName + "_" + num + "_cover.csv";

        PrintWriter br = new PrintWriter(new FileWriter(outFileName));
        for (BitSet bs : printedAlready) {
            for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i + 1)) {
                br.print(i + " ");
            }
            br.println();
        }
        br.flush();
        br.close();
    }

    public static void writeHypergraphToFile(
            List<IntSet> content, String fileRoot, int numOfVertex, int numOfEdge, String repairAbility) throws IOException {
        String file = String.format("%s/detect_v%d_e%d_%s_%s.txt", fileRoot, numOfVertex, numOfEdge,
                repairAbility, UUID.randomUUID().toString().replaceAll("-", ""));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for (IntSet intSet : content) {
            boolean first = true;
            for (int element : intSet.get()) {
                if (!first) bufferedWriter.write(" ");
                bufferedWriter.write(String.valueOf(element));
                first = false;
            }
            bufferedWriter.write("\n");
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }
}
