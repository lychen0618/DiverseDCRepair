package com.lychen.mhsGenerationFamily.algorithm;

import com.lychen.mhsGenerationFamily.model.HyperGraph;
import com.lychen.mhsGenerationFamily.model.IntSet;
import com.lychen.mhsGenerationFamily.util.BitSetHelpFunc;
import com.lychen.mhsGenerationFamily.util.MMCSUtil;

import java.util.*;

public class RandomGenerator extends MMCSBase {
    protected int backStep = 0;
    Random randomForMessUp = new Random();
    Random randomForBackStep = new Random();
    int coverIndex = 0;
    HyperGraph tempH = null;
    List<Integer> SList = new ArrayList<>();
    boolean isAutoInc = false;

    public RandomGenerator(HyperGraph H) {
        super(H);
    }

    void changeFailFlag() {
        failedFlag = (coverIndex == requiredNumberOfMhs);
    }

    protected void randomMMCS(BitSet cand, List<IntSet> crit) {
        if (failedFlag) return;
        if (uncov.isEmpty()) {
//            for (BitSet bs : mhsSet) {
//                if (BitSetHelpFunc.equal(bs, S)) return;
//            }
            if (isAutoInc) {
                if (numOfMhs == coverIndex) {
                    mhsSet.add(new BitSet());
                    ++numOfMhs;
                }
                mhsSet.get(coverIndex).or(S);
            } else mhsSet.get(coverIndex++).or(S);
            if (isAutoInc) failedFlag = true;
//            //
//            mhsSet.add((BitSet) S.clone());
//            ++numOfMhs;
//            //
            //backStep = randomForBackStep.nextInt(S.cardinality());
            //backStep = randomForBackStep.nextInt((S.cardinality() + 1) / 2) + S.cardinality() / 2;
            backStep = S.cardinality();
            //backStep = S.cardinality() / 2;
            //System.out.println("get one mhs");
            if (!isAutoInc) changeFailFlag();
            return;
        }

        BitSet candCopy = (BitSet) cand.clone();
        IntSet nextCoverEdge = getGoodEdgeToCover(candCopy);
        IntSet.andNot(candCopy, nextCoverEdge);

//        List<Integer> listOfUncovEdge = new LinkedList<>();
//        for (int vertex : nextCoverEdge.get()) {
//            if (listOfUncovEdge.size() == 0) listOfUncovEdge.add(vertex);
//            else listOfUncovEdge.add(randomForMessUp.nextInt(listOfUncovEdge.size() + 1), vertex);
//        }

        List<Integer> listOfUncovEdge = new ArrayList<>();
        for (int vertex : nextCoverEdge.get()) {
            if (listOfUncovEdge.size() == 0) listOfUncovEdge.add(vertex);
            else listOfUncovEdge.add(randomForMessUp.nextInt(listOfUncovEdge.size() + 1), vertex);
        }
        listOfUncovEdge.sort((o1, o2) -> H.getVertexHitting(o2).size() - H.getVertexHitting(o1).size());

        boolean flag = false;
        for (int vertex : listOfUncovEdge) {
            if (backStep != 0) break;
            if (!MMCSUtil.vertex_would_violate(crit, H.getVertexHitting(vertex))) {
                flag = true;
                try {
                    List<IntSet> critClone = new ArrayList<>();
                    for (IntSet intSet : crit) critClone.add(intSet.clone());
                    MMCSUtil.update_crit_and_uncov_new(critClone, uncov, H.getVertexHitting(vertex));
                    S.flip(vertex);
                    SList.add(vertex);
                    randomMMCS(candCopy, critClone);
                    if (failedFlag) return;
                    S.flip(vertex);
                    SList.remove(SList.size() - 1);
                    candCopy.set(vertex);
                    MMCSUtil.restore_crit_and_uncov_new(critClone, uncov);
                } catch (Exception ignored) {
                }
            }
        }

//        //test
//        for (IntSet intSet : crit) {
//            for (int edgeIndex : intSet.get()) {
//                IntSet temp = IntSet.and(H.getHyperEdge(edgeIndex), new IntSet(S));
//                if (temp.size() != 1) {
//                    System.out.println();
//                }
//            }
//        }
//        for (int w = uncov.nextSetBit(0); w != -1; w = uncov.nextSetBit(w + 1)) {
//            if (H.getHyperEdge(w).intersects(S)) {
//                System.out.println();
//            }
//        }

        //如果无法对nextEdge进行覆盖
        if (!flag) {
            //System.out.println("failure");
            nextCoverEdge = H.getHyperEdge(nextEdge);
//            listOfUncovEdge = new LinkedList<>();
//            for (int vertex : nextCoverEdge.get()) {
//                if (listOfUncovEdge.size() == 0) listOfUncovEdge.add(vertex);
//                else listOfUncovEdge.add(randomForMessUp.nextInt(listOfUncovEdge.size()), vertex);
//            }
            listOfUncovEdge = new ArrayList<>();
            for (int vertex : nextCoverEdge.get()) {
                if (listOfUncovEdge.size() == 0) listOfUncovEdge.add(vertex);
                else listOfUncovEdge.add(randomForMessUp.nextInt(listOfUncovEdge.size() + 1), vertex);
            }
            listOfUncovEdge.sort((o1, o2) -> H.getVertexHitting(o2).size() - H.getVertexHitting(o1).size());

            for (int vertex : listOfUncovEdge) {
                if (backStep != 0) break;
                try {
                    BitSet oldS = (BitSet) S.clone();
                    List<Integer> oldSList = new ArrayList<>(SList);
                    BitSet oldUncov = (BitSet) uncov.clone();
                    List<IntSet> critClone = new ArrayList<>();
                    for (IntSet intSet : crit) critClone.add(intSet.clone());
                    MMCSUtil.update_crit_and_uncov_new(critClone, uncov, H.getVertexHitting(vertex));
                    S.flip(vertex);
                    SList.add(vertex);
                    Map<Integer, Integer> critMap = new HashMap<>();
                    for (int i = 0; i < SList.size(); ++i) critMap.put(SList.get(i), i);
                    for (int i = 0; i < SList.size() - 1; ++i) {
                        if (critClone.get(i).size() == 0) {
                            //更新其他顶点的critical edges
                            IntSet curVHitting = H.getVertexHitting(SList.get(i));
                            for (int edgeIndex : curVHitting.get()) {
                                IntSet commonVertex = IntSet.and(H.getHyperEdge(edgeIndex), S);
                                if (commonVertex.size() == 2) {
                                    critClone.get(critMap.get(commonVertex.another(SList.get(i)))).add(edgeIndex);
                                }
                            }
                            S.flip(SList.get(i));
                        }
                    }
                    List<Integer> tempSList = new ArrayList<>();
                    List<IntSet> tempCritClone = new ArrayList<>();
                    for (int i = 0; i < SList.size(); ++i) {
                        if (critClone.get(i).size() != 0) {
                            tempSList.add(SList.get(i));
                            tempCritClone.add(critClone.get(i));
                        }
                    }
                    SList = tempSList;
                    critClone = tempCritClone;

//                    //test
//                    for (int i = 0; i < SList.size(); ++i) {
//                        for (int edgeIndex : critClone.get(i).get()) {
//                            IntSet temp = IntSet.and(H.getHyperEdge(edgeIndex), new IntSet(S));
//                            //System.out.println(temp.size());
//                            if (temp.size() != 1) {
//                                System.out.println();
//                            }
//                            assert temp.size() == 1;
//                            assert temp.get().get(0).equals(SList.get(i));
//                        }
//                    }

                    randomMMCS(candCopy, critClone);
                    if (failedFlag) return;
                    S = oldS;
                    if (S.size() != 0) {
                        //backStep = randomForBackStep.nextInt((S.cardinality() + 1));
                        //backStep = randomForBackStep.nextInt((S.cardinality() + 1) / 2) + S.cardinality() / 2 + 1;
                        backStep = S.cardinality() + 1;
                        //backStep = S.cardinality() / 2;
                    }
                    SList = oldSList;
                    uncov = oldUncov;
                } catch (Exception ignored) {
                }
            }
        }
        if (backStep != 0) backStep--;
    }

    @Override
    public List<BitSet> generateMHS() {
        tempH = H;
        H = null;
        List<HyperGraph> blockedHyperGraph = tempH.getBlockedHyperGraph();
        for (int i = 0; i < requiredNumberOfMhs; ++i) mhsSet.add(new BitSet());
        for (HyperGraph hyperGraph : blockedHyperGraph) {
            //System.out.println("----------");
            H = hyperGraph;
            coverIndex = 0;
            while (coverIndex < requiredNumberOfMhs) {
                commonInit();
                SList = new ArrayList<>();
                backStep = 0;
                failedFlag = false;
                randomMMCS(initialCand, new ArrayList<>());
                assert failedFlag || backStep == 0;
            }
        }
        numOfMhs = requiredNumberOfMhs;
        return mhsSet;
//        while (true) {
//            commonInit();
//            SList = new ArrayList<>();
//            backStep = 0;
//            randomMMCS(initialCand, new ArrayList<>());
//            if(failedFlag) break;
//        }
//        return mhsSet;
    }
}
