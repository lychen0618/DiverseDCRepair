package com.lychen.dataRepair.myDCRepair.model;

import java.util.Set;
import java.util.TreeSet;

public class Violation {

    private final int ruleId;
    public VioPair vo;

    public Violation(int ruleId, int i, int j) {
//        this.vid=vid;
        this.ruleId = ruleId;
        this.vo = new VioPair(i, j);
    }

    public int getRuleId() {
        return this.ruleId;
    }

    public VioPair getVo() {
        return this.vo;
    }

    public Set<Integer> getIDSet() {return vo.getIDSet();}

    public int getOtherTid(int i) {
        return vo.getTheOtherTid(i);
    }

    public int getPos(int i) {
        if (vo.pos1 == i) return 1;
        else return 2;
    }

    @Override
    public Violation clone() {
        return new Violation(ruleId, vo.pos1, vo.pos2);
    }

}

class VioPair {
    public int pos1;
    //pos2为-1表示单条元组
    public int pos2;

    public VioPair(int i, int j) {
        pos1 = i;
        pos2 = j;
    }

    public int getTheOtherTid(int i) {
        if (pos1 == i) return pos2;
        return pos1;
    }

    public Set<Integer> getIDSet() {
        Set<Integer> res = new TreeSet<>();
        res.add(pos1);
        res.add(pos2);
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof VioPair) {
//            VioPair vp = (VioPair) obj;
//            if(this.pos1.equals(vp.pos1)&&this.pos2.equals(vp.pos2)) return true;
            if (this.pos1 == ((VioPair) obj).pos1 && this.pos2 == ((VioPair) obj).pos2) return true;
            return this.pos1 == ((VioPair) obj).pos2 && this.pos2 == ((VioPair) obj).pos1;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return pos1 + pos2;
    }

    @Override
    public String toString() {
        return "(" + pos1 + "," + pos2 + ")";
    }
}
