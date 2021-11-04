package com.lychen.dataRepair.myDCRepair.enumerate;

import java.util.HashMap;
import java.util.Map;

public class TreeNode {
    public int element;
    public Map<Integer, TreeNode> children;

    public TreeNode(int element) {
        this.element = element;
        children = new HashMap<>();
    }
}
