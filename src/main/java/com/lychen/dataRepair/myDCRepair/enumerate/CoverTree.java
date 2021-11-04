package com.lychen.dataRepair.myDCRepair.enumerate;

import com.lychen.mhsGenerationFamily.model.IntSet;

import java.util.List;

public class CoverTree {
    public TreeNode root;

    public CoverTree(List<IntSet> covers) {
        root = new TreeNode(-1);
        for (IntSet cover : covers) {
            TreeNode cur = root;
            for (int vertex : cover.get()) {
                if (!cur.children.containsKey(vertex)) {
                    cur.children.put(vertex, new TreeNode(vertex));
                }
                cur = cur.children.get(vertex);
            }
        }
    }
    public int numberOfTreeNode = 0;

    public int getNumberOfTreeNode(){
        getNumberOfTreeNodeHelper(root);
        return numberOfTreeNode;
    }

    private void getNumberOfTreeNodeHelper(TreeNode curRoot){
        if(curRoot.element != -1) ++numberOfTreeNode;
        for(TreeNode child : curRoot.children.values()) getNumberOfTreeNodeHelper(child);
    }

//    @Override
//    public String toString() {
//        return toStringHelper(root).toString();
//    }
//
//    private StringBuilder toStringHelper(TreeNode subRoot) {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("[" + subRoot.element + ", ");
//        for (TreeNode child : subRoot.children.values()) stringBuilder.append(toStringHelper(child) + ", ");
//        stringBuilder.append("]");
//        return stringBuilder;
//    }
}
