package com.lychen.mhsGenerationFamily.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class HyperGraph {
    private BitSet vertices = new BitSet();
    private List<IntSet> hyperEdges = new ArrayList<>();
    private Map<Integer, IntSet> vertexHittings = null;

    public HyperGraph() {
    }

    public HyperGraph(int vertexNum, List<IntSet> hyperEdges) {
        vertices.flip(0, vertexNum);
        this.hyperEdges = hyperEdges;
    }

    public HyperGraph(List<IntSet> hyperEdges) {
        this.hyperEdges = hyperEdges;
        for (IntSet intSet : hyperEdges) {
            for (int element : intSet.get()) {
                this.vertices.set(element);
            }
        }
    }

    public HyperGraph(Set<Integer> vertexSet, List<IntSet> hyperEdges) {
        for (int vertex : vertexSet) this.vertices.set(vertex);
        this.hyperEdges = hyperEdges;
    }

    public HyperGraph(String filePath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
        // vertexIndex may be not from 0 to numberOfVertex - 1; edgeIndex is from 0 to numberOfEdge - 1
        Map<Integer, List<Integer>> vertex_hittings = new HashMap<>();
        String line;
        for (int edgeIndex = 0; (line = bufferedReader.readLine()) != null; ++edgeIndex) {
            String[] verticesString = line.split(" ");
            hyperEdges.add(new IntSet());
            for (String vertexString : verticesString) {
                int vertexInt = Integer.parseInt(vertexString);
                while (!vertex_hittings.containsKey(vertexInt)) {
                    vertex_hittings.put(vertexInt, new ArrayList<>());
                }
                vertex_hittings.get(vertexInt).add(edgeIndex);
            }
        }
        for (int vertex : vertex_hittings.keySet()) this.vertices.set(vertex);
        for (int vertexIndex : vertex_hittings.keySet()) {
            for (int edgeIndex : vertex_hittings.get(vertexIndex)) {
                hyperEdges.get(edgeIndex).add(vertexIndex);
            }
        }
    }

    /**
     * 返回包含vertex的超边的集合
     */
    public IntSet getVertexHitting(int vertex) {
        if (vertexHittings == null) {
            vertexHittings = new HashMap<>();
            int[] vertices = this.vertices.stream().toArray();
            for (int vertexIndex : vertices) vertexHittings.put(vertexIndex, new IntSet());
            for (int edgeIndex = 0; edgeIndex < hyperEdges.size(); ++edgeIndex) {
                for (int containedVertex : hyperEdges.get(edgeIndex).get()) {
                    vertexHittings.get(containedVertex).add(edgeIndex);
                }
            }
        }
        return vertexHittings.get(vertex);
    }

    public void addAnotherHyperEdges(List<IntSet> hyperEdges) {
        this.hyperEdges.addAll(hyperEdges);
        for (IntSet intSet : hyperEdges) {
            for (int element : intSet.get()) this.vertices.set(element);
        }
    }

    /**
     * 压缩超图，去除大超边后的超图
     */
    public void compressHyperGraph() {
        List<HyperGraph> blockedHyperGraph = getBlockedHyperGraph();
        List<IntSet> remainEdges = new ArrayList<>();

        for(HyperGraph bh : blockedHyperGraph){
            BitSet edge = new BitSet();
            //long startTime = System.currentTimeMillis();
            List<BitSet> bitSetList = new ArrayList<>();
            for (IntSet e : bh.getAllHyperEdges()) {
                bitSetList.add(IntSet.transformIntSetToBitSet(e));
            }
            for (int edgeIndex = 0; edgeIndex < bh.getNumOfHyperEdge(); ++edgeIndex) {
                boolean flag = true;
                for (int anotherEdgeIndex = 0; anotherEdgeIndex < bh.getNumOfHyperEdge(); ++anotherEdgeIndex) {
                    if (anotherEdgeIndex == edgeIndex) continue;
                    if (bh.getHyperEdge(edgeIndex).size() < bh.getHyperEdge(anotherEdgeIndex).size()) continue;
                    if (bh.getHyperEdge(anotherEdgeIndex).isSubsetOf(bitSetList.get(edgeIndex))) {
                        if (!(bh.getHyperEdge(anotherEdgeIndex).isEqual(bitSetList.get(edgeIndex))
                                && anotherEdgeIndex > edgeIndex)) {
                            flag = false;
                            break;
                        }
                    }
                }
                if (flag) edge.set(edgeIndex);
            }

            //对比
//        for (int edgeIndex = 0; edgeIndex < H.getNumOfHyperEdge(); ++edgeIndex) {
//            boolean flag = true;
//            for(int anotherEdgeIndex = 0; anotherEdgeIndex < H.getNumOfHyperEdge(); ++anotherEdgeIndex){
//                if(anotherEdgeIndex == edgeIndex) continue;
//                if(H.getHyperEdge(edgeIndex).size() < H.getHyperEdge(anotherEdgeIndex).size()) continue;
//                if(H.getHyperEdge(anotherEdgeIndex).isSubsetOf(H.getHyperEdge(edgeIndex))){
//                    flag = false;
//                    break;
//                }
//            }
//            if(flag) edge.set(edgeIndex);
//        }
//        System.out.println("time cost:" + (System.currentTimeMillis() - startTime));
            try {
                for (int edgeIndex : edge.stream().toArray()) {
                    remainEdges.add(bh.getHyperEdge(edgeIndex));
                }
            } catch (Exception ignored) {
            }
            System.out.println("before compress edgeNum: " + bh.getNumOfHyperEdge());
            System.out.println("after compress edgeNum: " + edge.cardinality());
        }
        hyperEdges = remainEdges;
        vertexHittings = null;
    }

    public List<HyperGraph> getBlockedHyperGraph() {
        List<HyperGraph> blockedHyperGraph = new ArrayList<>();
        List<Boolean> cellCoverFlag = new ArrayList<>();
        List<Boolean> vioSolveFlag = new ArrayList<>();
        for (int cellIndex = 0; cellIndex < this.getNumOfVertex(); ++cellIndex) cellCoverFlag.add(false);
        for (int vioIndex = 0; vioIndex < this.getNumOfHyperEdge(); ++vioIndex) vioSolveFlag.add(false);
        for (int cellIndex = 0; cellIndex < this.getNumOfVertex(); ++cellIndex) {
            if (cellCoverFlag.get(cellIndex)) continue;
            Set<Integer> cellIndexSet = new HashSet<>();
            List<IntSet> violationSet = new ArrayList<>();
            getBlockHelpFunc(cellIndex, cellIndexSet, violationSet, cellCoverFlag, vioSolveFlag);
            blockedHyperGraph.add(new HyperGraph(cellIndexSet, violationSet));
            //System.out.printf("%d block of hypergraph: vertex/edge %d/%d%n",
                    //blockedHyperGraph.size(), cellIndexSet.size(), violationSet.size());
        }
        for (boolean b : cellCoverFlag) assert b;
        for (boolean b : vioSolveFlag) assert b;
        System.out.println("block finish!");
        return blockedHyperGraph;
    }

    private void getBlockHelpFunc(int cellIndex, Set<Integer> cellIndexSet, List<IntSet> violationSet,
                                  List<Boolean> cellCoverFlag, List<Boolean> vioSolveFlag) {
        if (cellCoverFlag.get(cellIndex)) return;
        cellIndexSet.add(cellIndex);
        cellCoverFlag.set(cellIndex, true);
        for (int vioIndex : getVertexHitting(cellIndex).get()) {
            if (vioSolveFlag.get(vioIndex)) continue;
            violationSet.add(this.getHyperEdge(vioIndex));
            vioSolveFlag.set(vioIndex, true);
            for (int newCellIndex : this.getHyperEdge(vioIndex).get()) {
                getBlockHelpFunc(newCellIndex, cellIndexSet, violationSet, cellCoverFlag, vioSolveFlag);
            }
        }
    }

    /**
     * 超图分块
     */
    public void blockHyperGraph() {
        BitSet vertexCoverFlag = new BitSet();
        BitSet edgeSolveFlag = new BitSet();
        int numOfBlock = 0;
        for (int vertex : vertices.stream().toArray()) {
            if (vertexCoverFlag.get(vertex)) continue;
            Set<Integer> vertexSet = new HashSet<>();
            List<IntSet> edgeSet = new ArrayList<>();
            getBlockHelpFunc(vertex, vertexSet, edgeSet, vertexCoverFlag, edgeSolveFlag);
            ++numOfBlock;
        }
        System.out.println("block number: " + numOfBlock);
    }

    private void getBlockHelpFunc(int vertex, Set<Integer> vertexSet, List<IntSet> edgeSet,
                                  BitSet vertexCoverFlag, BitSet edgeSolveFlag) {
        if (vertexCoverFlag.get(vertex)) return;
        vertexSet.add(vertex);
        vertexCoverFlag.set(vertex, true);
        for (int edgeIndex : getVertexHitting(vertex).get()) {
            if (edgeSolveFlag.get(edgeIndex)) continue;
            edgeSet.add(hyperEdges.get(edgeIndex));
            edgeSolveFlag.set(edgeIndex, true);
            for (int newVertexIndex : hyperEdges.get(edgeIndex).get()) {
                getBlockHelpFunc(newVertexIndex, vertexSet, edgeSet, vertexCoverFlag, edgeSolveFlag);
            }
        }
    }

    @Override
    public HyperGraph clone() throws CloneNotSupportedException {
        HyperGraph clone = (HyperGraph) super.clone();
        clone.setVertices(vertices);
        clone.hyperEdges = new ArrayList<>();
        for (IntSet intSet : hyperEdges) {
            clone.hyperEdges.add(intSet.clone());
        }
        return clone;
    }

    public int getNumOfVertex() {
        return vertices.cardinality();
    }

    public int getNumOfHyperEdge() {
        return hyperEdges.size();
    }

    /**
     * 得到含有的顶点的序号
     */
    public BitSet getVertices() {
        return vertices;
    }

    public void setVertices(BitSet vertices) {
        this.vertices = (BitSet) vertices.clone();
    }

    /**
     * 返回指定序号的超边
     */
    public IntSet getHyperEdge(int edgeIndex) {
        return hyperEdges.get(edgeIndex);
    }

    /**
     * 返回所有超边的集合
     */
    public List<IntSet> getAllHyperEdges() {
        return hyperEdges;
    }
}
