package com.lychen.dataRepair.myDCRepair.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rule {
    private final int ruleId;
    private final boolean isSingle;
    private final String dc;
    private final List<Predicate> predicates;
    private final Set<Predicate> equalPredicates;
    private boolean containNumAttrs;
    private final Set<String> attrs;

    public Rule(int ruleId, boolean isSingle, String dc) {
        this.ruleId = ruleId;
        this.isSingle = isSingle;
        this.dc = dc;

        predicates = new ArrayList<>();
        for (String atom : dc.substring(4, dc.length() - 1).split("&")) {
            Predicate predicate = new Predicate(atom);
            predicates.add(predicate);
        }

        equalPredicates = new HashSet<>();
        for (Predicate predicate : this.getAtoms()) {
            if (predicate.getOp().equals("=") && predicate.getLeftAttr().equals(predicate.getRightAttr()))
                equalPredicates.add(predicate);
        }

        containNumAttrs = false;
        for (Predicate predicate : predicates) {
            if (!predicate.getOp().equals("=") && !predicate.getOp().equals("!=")) {
                containNumAttrs = true;
                break;
            }
        }

        attrs = new HashSet<>();
        for (Predicate predicate : predicates) {
            if (!predicate.getIsSingle()) attrs.add(predicate.getRightAttr());
            attrs.add(predicate.getLeftAttr());
        }
    }

    public int getRuleId() {
        return ruleId;
    }

    public List<Predicate> getAtoms() {
        return predicates;
    }

    public boolean hasAttr(String attr) {
        return attrs.contains(attr);
    }

    public boolean getIsSingle() {
        return isSingle;
    }

    /**
     * return the set of predicates with common attribute in left and right and the operation is "="
     */
    public Set<Predicate> getEqualPredicates() {
        return equalPredicates;
    }

    public Set<String> getAttrs() {
        return attrs;
    }

    public boolean hasNumAttrs() {
        return containNumAttrs;
    }

    public boolean containEQPredicate() {
        for (Predicate pre : predicates) {
            if (pre.getOp().equals("=")) return true;
        }
        return false;
    }

    public boolean onlyContainEQPredicate() {
        for (Predicate pre : predicates) {
            if (!pre.getOp().equals("=")) return false;
        }
        return true;
    }

    public List<String> getEQAttr() {
        List<String> res = new ArrayList<>();
        for (Predicate pre : predicates) {
            if (pre.getOp().equals("=")) res.add(pre.getLeftAttr());
        }
        return res;
    }

    @Override
    public String toString() {
        return this.dc;
    }
}
