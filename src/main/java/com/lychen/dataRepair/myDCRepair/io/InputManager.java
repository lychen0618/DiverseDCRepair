package com.lychen.dataRepair.myDCRepair.io;

import com.lychen.dataRepair.myDCRepair.model.Cell;
import com.lychen.dataRepair.myDCRepair.model.Predicate;
import com.lychen.dataRepair.myDCRepair.model.Rule;
import com.lychen.dataRepair.myDCRepair.model.Table;
import com.lychen.dataRepair.myDCRepair.util.Util;
import com.lychen.dataRepair.myDCRepair.util.Config;

import java.io.*;
import java.util.*;

public class InputManager {
    public String datasetName, tupleNum, noisyMethod;
    public double errorRate;
    public String repairAbility;
    public int tupleID = 0;
    public Table table;
    /**
     * the value of a pair <key, value> in map is the original value of the error cell(key)
     */
    public Map<Cell, String> noisyCells = new HashMap<>();
    public final List<Rule> rules = new ArrayList<>();
    /**
     * single tuple rules
     */
    public final List<Integer> singleRules = new ArrayList<>();
    /**
     * the first string is the column attribute; the second string is the corresponding value
     * the list contains the ids of tuples with common attribute value
     */
    public Map<String, Map<String, List<Integer>>> equivalenceMap;
    public Map<Rule, List<Integer>> hashTableForARule = new HashMap<>();
    public Config config;

    /**
     * use a simple(ad-hoc) method to introduce errors
     */
    public InputManager(String datasetName, String tupleNum, String noisyMethod, double errorRate) throws IOException {
        this.datasetName = datasetName;
        this.tupleNum = tupleNum;
        this.errorRate = errorRate;
        this.noisyMethod = noisyMethod;
        initialize();
        addNoisy();
    }

    /**
     * read the errors introduced by BART
     */
    public InputManager(String datasetName, String tupleNum, double errorRate, String repairAbility) throws IOException {
        readConfig();
        this.datasetName = datasetName;
        this.tupleNum = tupleNum;
        this.errorRate = errorRate;
        this.repairAbility = repairAbility;
        initialize();
        noisyCells = readNoisyCellsFromFile();
        getHashTableForAllRule();
    }

    public void rebuildEquivalenceMapAndHashTable() {
        equivalenceMap = new HashMap<>();
        for (int i = 0; i < table.getAttrs().length; ++i) equivalenceMap.put(table.getAttrs()[i], new HashMap<>());
        int tupleID = 0;
        for (List<Cell> tuple : table.getTuples()) {
            for (int i = 0; i < tuple.size(); ++i) {
                String attr = table.getAttrs()[i];
                String value = tuple.get(i).getValue();
                if (!equivalenceMap.get(attr).containsKey(value))
                    equivalenceMap.get(attr).put(value, new ArrayList<>());
                equivalenceMap.get(attr).get(value).add(tupleID);
            }
            ++tupleID;
        }
        getHashTableForAllRule();
    }

    /**
     * read config information
     */
    public void readConfig() throws IOException {
        String configPath = "./src/main/java/com/lychen/dataRepair/config.txt";
        BufferedReader reader = new BufferedReader(new FileReader(configPath));
        String line;
        Map<String, Object> keyValue = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            //if(line.charAt(0)=='#') continue;
            keyValue.put(line.split(":")[0], line.split(":")[1]);
        }

        Config.Builder builder = new Config.Builder();
        if (keyValue.containsKey("debug")) {
            boolean val = Boolean.parseBoolean((String) keyValue.get("debug"));
            if (val) builder.setMode("debug");
        }
        config = builder.builder();
    }

    /**
     * initialize: read rules, creat table, add noise, build equalence class
     */
    private void initialize() throws IOException {
        getRules(datasetName);
        createTable();
    }

    /**
     * read the error cells introduced by BART
     */
    private Map<Cell, String> readNoisyCellsFromFile() throws IOException {
        String prefix = "./data/input/dataset/";
        String filePathPrefix = prefix + datasetName + "/" + tupleNum + "/out/" + tupleNum + "_" + (int) errorRate + "%_" + repairAbility + "/";
        String filePath = filePathPrefix + "changes.csv";
        String oldHeaderLine = new BufferedReader(new FileReader(prefix + datasetName + ".csv")).readLine();
        List<String> oldHeader = new ArrayList<>();
        for (String column : oldHeaderLine.split(",")) oldHeader.add(column.split(" ")[0]);
        String newHeaderLine = new BufferedReader(new FileReader(filePathPrefix + "dirty_" + datasetName + ".csv")).readLine();
        List<String> newHeader = new ArrayList<>();
        for (String column : newHeaderLine.split(",")) {
            if (column.contains("(")) newHeader.add(column.substring(0, column.indexOf("(")));
            else newHeader.add(column);
        }
        Map<Cell, String> res = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] strs = line.split(",");
            String[] pointSplit = strs[0].split("\\.");
            // System.out.println(pointSplit.length + " " + pointSplit[0]);
            int tid = Integer.parseInt(pointSplit[0]) - 1;
            int pos = newHeader.indexOf(pointSplit[1]);
            if (pos == -1) {
                System.out.println();
            }
            String attr = oldHeader.get(pos);
            res.put(table.getCell(tid, attr), strs[2]);
        }
        return res;
    }

    /**
     * use the ad-hoc method to add noise
     */
    private void addNoisy() {
        noisyCells = Util.addNoise(noisyMethod, errorRate * 1.0 * table.getAttrsSize() / (table.header.split(",").length), table);
        System.out.println("The number of error cells: " + noisyCells.size());
    }

    /**
     * create table, initialize equivalence class
     */
    private void createTable() throws IOException {
        String prefix = "./data/input/dataset/";
        String filePathPrefix = prefix + datasetName + "/" + tupleNum + "/out/" + tupleNum + "_" + (int) errorRate + "%_" + repairAbility + "/";
        String filePath = filePathPrefix + "dirty_" + datasetName + ".csv";
        BufferedReader headReader = new BufferedReader(new FileReader(prefix + datasetName + ".csv"));
        String header = headReader.readLine();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        reader.readLine();
        table = new Table(header, getNeededColumns(rules));
        String line;
        while ((line = reader.readLine()) != null) {
            table.addTuple(line, tupleID);
            tupleID++;
        }
        equivalenceMap = table.getEquivalenceMap();
        headReader.close();
        reader.close();
    }

    /**
     * read rules
     */
    private void getRules(String dataset) throws IOException {
        String filename = "./data/input/rule/" + dataset + "_rules.csv";
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] rule = line.split(",");
            rules.add(new Rule(rules.size(), !rule[1].equals("2"), rule[0]));
        }
        for (Rule rule : rules) {
            if (rule.getIsSingle()) singleRules.add(rule.getRuleId());
        }
    }

    /**
     * get attributes related to rules
     */
    private Set<String> getNeededColumns(List<Rule> rules) {
        Set<String> allAttrs = new HashSet<>();
        for (Rule rule : rules) {
            for (Predicate predicate : rule.getAtoms()) {
                allAttrs.addAll(predicate.getAttrs());
            }
        }
        return allAttrs;
    }

    public void getHashTableForAllRule() {
        hashTableForARule = new HashMap<>();
        for (Rule rule : rules) {
            if (singleRules.contains(rule.getRuleId())) continue;
            List<Integer> hashTable = new ArrayList<>();
            if (rule.getAttrs().size() == 2) {
                Map<String, Map<String, Integer>> map = new HashMap<>();
                int hashIndex = 0;
                for (int i = 0; i < table.getTupleSize(); ++i) {
                    Iterator<String> it = rule.getAttrs().iterator();
                    String val1 = table.getCellValue(i, it.next());
                    String val2 = table.getCellValue(i, it.next());
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
                    hashTable.add(hashcode);
                }
            } else if (rule.getAttrs().size() == 3) {
                Map<String, Map<String, Map<String, Integer>>> map = new HashMap<>();
                int hashIndex = 0;
                for (int i = 0; i < table.getTupleSize(); ++i) {
                    Iterator<String> it = rule.getAttrs().iterator();
                    String val1 = table.getCellValue(i, it.next());
                    String val2 = table.getCellValue(i, it.next());
                    String val3 = table.getCellValue(i, it.next());
                    int hashcode;
                    if (!map.containsKey(val1)) {
                        Map<String, Map<String, Integer>> temp = new HashMap<>();
                        hashcode = hashIndex++;
                        temp.put(val2, new HashMap<String, Integer>() {{
                            put(val3, hashcode);
                        }});
                        map.put(val1, temp);
                    } else {
                        Map<String, Map<String, Integer>> map2 = map.get(val1);
                        if (!map2.containsKey(val2)) {
                            hashcode = hashIndex++;
                            map2.put(val2, new HashMap<String, Integer>() {{
                                put(val3, hashcode);
                            }});
                        } else {
                            Map<String, Integer> map3 = map2.get(val2);
                            if (!map3.containsKey(val3)) {
                                hashcode = hashIndex++;
                                map3.put(val3, hashcode);
                            } else {
                                hashcode = map3.get(val3);
                            }
                        }
                    }
                    hashTable.add(hashcode);
                }
            } else {
                System.out.println("A rule can't contain more than 3 attributes!");
                System.exit(-1);
            }
            hashTableForARule.put(rule, hashTable);
        }
    }
}
