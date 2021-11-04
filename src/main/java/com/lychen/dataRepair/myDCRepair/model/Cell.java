package com.lychen.dataRepair.myDCRepair.model;

public class Cell {
    //the id of the tuple it belongs
    private Integer tid;
    //attr: in lower case
    private String attr;
    private String value;

    public Cell(Integer tid,String attr,String value){
        this.tid=tid;
        this.attr=attr;
        this.value=value;
    }

    public Integer getTid(){
        return tid;
    }

    public String getAttr(){
        return attr;
    }

    public String getValue(){
        return value;
    }

    public void setValue(String val){
        this.value=val;
    }

    @Override
    public String toString(){
        return tid+"."+attr+"="+value;
    }

    @Override
    public boolean equals(Object obj){
        if(obj==null) return false;
        if(this==obj) return true;
        if(obj instanceof Cell){
//            VioPair vp=(VioPair) obj;
            Cell cell=(Cell) obj;
//            if(this.pos1.equals(vp.pos1)&&this.pos2.equals(vp.pos2)) return true;
            if(cell.attr.equals(this.attr)&&cell.tid.equals(this.tid)) return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return this.tid+this.attr.hashCode();
    }
}

