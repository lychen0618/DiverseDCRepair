package com.lychen.dataRepair.myDCRepair.model;

public class RepairPair {
    public String cell;
    public String oldValue;
    public String newValue;

    public RepairPair(Cell cell, String oldValue) {
        this.cell = cell.getTid() + "." + cell.getAttr();
        this.oldValue = oldValue;
        this.newValue = cell.getValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof RepairPair) {
            RepairPair repairPair = (RepairPair) obj;
            return repairPair.cell.equals(this.cell) && repairPair.newValue.equals(this.newValue)
                    && repairPair.oldValue.equals(oldValue);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.cell.hashCode() + this.oldValue.hashCode() + this.newValue.hashCode();
    }
}
