package com.lychen.dataRepair.myDCRepair.model;

import java.util.HashSet;
import java.util.Set;

public class Fix {
    private boolean stringType;
    private String leftBound = "";
    private String rightBound = "";
    private final boolean[] inclusive = new boolean[2];
    private final Set<String> notEQValues = new HashSet<>();

    private boolean hasRepair = true;

    public Fix() {
        stringType = true;
    }

    public Fix(Predicate predicate) {
        String op = predicate.getOp();
        stringType = (op.equals("=")) || (op.equals("!="));
    }

    public String getLowerBound() {
        return leftBound;
    }

    public String getUpperBound() {
        return rightBound;
    }

    public boolean getIsStr() {
        return stringType;
    }

    public boolean[] getInclusive() {
        return inclusive;
    }

    public Set<String> getNotEQValues() {
        return notEQValues;
    }

    public void setLowerBound(String lowerBound) {
        this.leftBound = lowerBound;
    }

    public void setUpperBound(String upperBound) {
        this.rightBound = upperBound;
    }

    public void setIsStr(boolean isStr) {
        stringType = isStr;
    }

    public boolean changeBound(String op, String value) {
        assert hasRepair;
        if (stringType) {
            if (op.equals("!=")) {
                if (!leftBound.isEmpty() && leftBound.equals(value)) hasRepair = false;
                else notEQValues.add(value);
            } else {
                if (!leftBound.isEmpty()) hasRepair = leftBound.equals(value);
                else {
                    leftBound = value;
                    hasRepair = !notEQValues.contains(value);
                }
            }
        } else {
            switch (op) {
                case "<":
                    if (rightBound.isEmpty()) {
                        rightBound = value;
                        inclusive[1] = false;
                    } else {
                        double doubleValue = Double.parseDouble(value);
                        double temp = Double.parseDouble(rightBound);
                        if (temp == doubleValue) inclusive[1] = false;
                        else if (temp > doubleValue) {
                            inclusive[1] = false;
                            rightBound = value;
                        }
                    }
                    if (!leftBound.isEmpty()) isConflict();
                    break;
                case "<=":
                    if (rightBound.isEmpty()) {
                        rightBound = value;
                        inclusive[1] = true;
                    } else {
                        double doubleValue = Double.parseDouble(value);
                        double temp = Double.parseDouble(rightBound);
                        if (temp > doubleValue) {
                            inclusive[1] = true;
                            rightBound = value;
                        }
                    }
                    if (!leftBound.isEmpty()) isConflict();
                    break;
                case ">":
                    if (leftBound.isEmpty()) {
                        leftBound = value;
                        inclusive[0] = false;
                    } else {
                        double doubleValue = Double.parseDouble(value);
                        double temp = Double.parseDouble(leftBound);
                        if (temp == doubleValue) inclusive[0] = false;
                        else if (temp < doubleValue) {
                            inclusive[0] = false;
                            leftBound = value;
                        }
                    }
                    if (!rightBound.isEmpty()) isConflict();
                    break;
                case ">=":
                    if (leftBound.isEmpty()) {
                        leftBound = value;
                        inclusive[0] = true;
                    } else {
                        double doubleValue = Double.parseDouble(value);
                        double temp = Double.parseDouble(leftBound);
                        if (temp < doubleValue) {
                            inclusive[0] = true;
                            leftBound = value;
                        }
                    }
                    if (!rightBound.isEmpty()) isConflict();
                    break;
                default:
                    break;
            }
        }
        return hasRepair;
    }

    //更新修复
    public boolean combineAnotherFix(Fix newFix) {
        assert hasRepair;
        this.stringType = newFix.stringType;
        if (!newFix.hasRepair()) hasRepair = false;
        else {
            String anotherLeftBound = newFix.getLowerBound();
            if (stringType) {
                if (!leftBound.isEmpty() && !anotherLeftBound.isEmpty()) {
                    if (!leftBound.equals(anotherLeftBound)) hasRepair = false;
                } else {
                    notEQValues.addAll(newFix.getNotEQValues());
                    if (!anotherLeftBound.isEmpty()) leftBound = anotherLeftBound;
                    if (!leftBound.isEmpty()) hasRepair = !notEQValues.contains(leftBound);
                }
            } else {
                if (!anotherLeftBound.isEmpty()) {
                    if (leftBound.isEmpty()) {
                        leftBound = anotherLeftBound;
                        inclusive[0] = newFix.getInclusive()[0];
                    } else {
                        if (inclusive[0] && newFix.getInclusive()[0]) {
                            if (Double.parseDouble(leftBound) < Double.parseDouble(anotherLeftBound))
                                leftBound = anotherLeftBound;
                        } else {
                            if (Double.parseDouble(leftBound) < Double.parseDouble(anotherLeftBound)) {
                                leftBound = anotherLeftBound;
                                inclusive[0] = newFix.getInclusive()[0];
                            } else if (Double.parseDouble(leftBound) == Double.parseDouble(anotherLeftBound))
                                inclusive[0] = false;
                        }
                    }
                }
                if (!leftBound.isEmpty() && !rightBound.isEmpty()) isConflict();
                if (hasRepair) {
                    String anotherRightBound = newFix.getUpperBound();
                    if (!anotherRightBound.isEmpty()) {
                        if (rightBound.isEmpty()) {
                            rightBound = anotherRightBound;
                            inclusive[1] = newFix.getInclusive()[1];
                        } else {
                            if (inclusive[1] && newFix.getInclusive()[1]) {
                                if (Double.parseDouble(rightBound) > Double.parseDouble(anotherRightBound))
                                    rightBound = anotherRightBound;
                            } else {
                                if (Double.parseDouble(rightBound) > Double.parseDouble(anotherRightBound)) {
                                    rightBound = anotherRightBound;
                                    inclusive[1] = newFix.getInclusive()[1];
                                } else if (Double.parseDouble(rightBound) == Double.parseDouble(anotherRightBound))
                                    inclusive[1] = false;
                            }
                        }
                    }
                    if (!leftBound.isEmpty() && !rightBound.isEmpty()) isConflict();
                }
            }
        }
        return hasRepair;
    }

    public boolean isInBound(String v) {
        if (stringType) {
            return !notEQValues.contains(v);
        } else {
            double doubleValue = Double.parseDouble(v);
            if (!leftBound.isEmpty()) {
                double leftBoundDouble = Double.parseDouble(leftBound);
                if (doubleValue < leftBoundDouble || (doubleValue == leftBoundDouble && !inclusive[0])) return false;
            }
            if (!rightBound.isEmpty()) {
                double rightBoundDouble = Double.parseDouble(rightBound);
                return !(doubleValue > rightBoundDouble) && ((doubleValue != rightBoundDouble) || inclusive[1]);
            }
        }
        return true;
    }

    private void isConflict() {
        hasRepair = (inclusive[0] && inclusive[1]) ?
                (Double.parseDouble(leftBound) <= Double.parseDouble(rightBound)) :
                (Double.parseDouble(leftBound) < Double.parseDouble(rightBound));
    }

    public boolean hasRepair() {
        return hasRepair;
    }

    @Override
    public String toString() {
        return "<" + leftBound + "," + rightBound + ">";
    }

    @Override
    public int hashCode() {
        return this.leftBound.hashCode() + this.rightBound.hashCode();
    }

//    @Override
//    public boolean equals(Object obj){
//        if(obj==null) return false;
//        if(this==obj) return true;
//        if(obj instanceof Fix){
//            Fix fix=(Fix) obj;
//            if(fix.lowerBound.equals(this.lowerBound)&&fix.upperBound.equals(this.upperBound)) return true;
//        }
//        return false;
//    }
}
