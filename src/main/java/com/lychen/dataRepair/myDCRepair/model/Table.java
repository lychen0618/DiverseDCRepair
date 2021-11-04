package com.lychen.dataRepair.myDCRepair.model;

import java.util.*;

public class Table {
    public String header;
    private String[] attrs;
    //如果属性的类型为String，attrType为true
    private boolean[] attrType;
    //attrIndex[i]:规则涉及的第i个属性在header中的序号
    private int[] attrIndex;
    //attr -> attrIndex
    private Map<String, Integer> attrMapIndex = new HashMap<>();

    private final List<List<Cell>> tuples = new ArrayList<>();

    private Map<String, Map<String, List<Integer>>> equivalenceMap = new HashMap<>();

    public Table(String header, Set<String> allAttrs) {
        this.header = header;
        int len = allAttrs.size();
        this.attrIndex = new int[len];
        String[] columns = header.split(",");
        this.attrs = new String[len];
        this.attrType = new boolean[len];
        for (int i = 0, index = 0; i < columns.length; i++) {
            String columnAttr = columns[i].split(" ")[0];
            if (!allAttrs.contains(columnAttr)) continue;
            if (columns[i].contains("varchar")) attrType[index] = true;
            attrIndex[index] = i;
            attrs[index] = columnAttr;
            attrMapIndex.put(columnAttr, index);
            index++;
        }
        for (String str : attrs) {
            equivalenceMap.put(str, new HashMap<>());
//            System.out.println(str);
        }
    }

    public void addTuple(String tupleLine, int tid) {
        List<Cell> lineContent = new ArrayList<>();
        String[] temp = tupleLine.split(",");
//        int tid = tuples.size();
        for (int i = 0; i < attrs.length; i++) {
            String value = temp[attrIndex[i]];
            lineContent.add(new Cell(tid, attrs[i], value));
            Map<String, List<Integer>> attrMap = equivalenceMap.get(attrs[i]);
            if (attrMap.containsKey(value)) {
                attrMap.get(value).add(tid);
            } else {
                List<Integer> list = new ArrayList<>();
                list.add(tid);
                attrMap.put(value, list);
            }
        }
        tuples.add(lineContent);
    }

    /**
     * return the value of the cell
     */
    public String getCellValue(Integer tid, String attr) {
        if (tid < tuples.size()) {
            List<Cell> tuple = tuples.get(tid);
            return tuple.get(attrMapIndex.get(attr)).getValue();
        } else {
            return null;
        }
    }

    public Cell getCell(Integer tid, String attr) {
        return tuples.get(tid).get(attrMapIndex.get(attr));
    }

    public int getTupleSize() {
        return tuples.size();
    }

    public Map<String, Map<String, List<Integer>>> getEquivalenceMap() {
        return equivalenceMap;
    }

    public int getAttrsSize() {
        return attrs.length;
    }

    public String[] getAttrs() {
        return attrs;
    }

    public List<List<Cell>> getTuples() {
        return tuples;
    }

    public boolean[] getAttrType() {
        return attrType;
    }
}
