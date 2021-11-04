package com.lychen.dataRepair.myDCRepair.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Predicate {
    //isSingle = true表示当前predicate的rightAttr为常量，不含t1或t2
    private boolean isSingle;
    private String op;
    private String leftAttr;
    private String rightAttr;
    private String predToString;

    private static Map<String, String> reverseAtom = new HashMap<String, String>() {
        {
            put("=", "!=");
            put("!=", "=");
            put("<", ">=");
            put(">", "<=");
            put(">=", "<");
            put("<=", ">");
        }
    };

    private static Map<String, String> revertAtom = new HashMap<String, String>() {
        {
            put("=", "!=");
            put("!=", "=");
            put("<", "<=");
            put(">", ">=");
            put(">=", ">");
            put("<=", "<");
        }
    };

    public Predicate() {
    }

    public Predicate(String predicate) {
        predToString = predicate;
        isSingle=false;
        if (predicate.contains("!=")) op = "!=";
        else if (predicate.contains("<=")) op = "<=";
        else if (predicate.contains(">=")) op = ">=";
        else if (predicate.contains("<")) op = "<";
        else if (predicate.contains(">")) op = ">";
        else op = "=";
        String leftVal = predicate.substring(0, predicate.indexOf(op));
        String rightVal = predicate.substring(predicate.indexOf(op) + op.length());
        leftAttr = leftVal.substring(3);
        if (!rightVal.contains("t2.")&&!rightVal.contains("t1.")){
            rightAttr = rightVal;
            isSingle=true;
        }
        else rightAttr = rightVal.substring(3);
    }

    public String getReversedOp(){
        return Predicate.reverseAtom.get(op);
    }

    public String getRevertOp(){
        return Predicate.revertAtom.get(op);
    }

    public boolean getIsSingle() {
        return isSingle;
    }

    public String getOp() {
        return op;
    }

    public String getLeftAttr() {
        return leftAttr;
    }

    public String getRightAttr() {
        return rightAttr;
    }

    //op是否是=或 ！=
    public boolean isStr() {
        return op.equals("=") || op.equals("!=");
    }

    public List<String> getAttrs() {
        List<String> res = new ArrayList<>();
        res.add(leftAttr);
        if (!isSingle && !leftAttr.equals(rightAttr)) res.add(rightAttr);
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof Predicate) {
            Predicate pred = (Predicate) obj;
            return pred.predToString.equals(((Predicate) obj).predToString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return predToString.hashCode();
    }

    @Override
    public String toString() {
        return predToString;
    }
}

