package com.lychen.mhsGenerationFamily.model;

import com.lychen.mhsGenerationFamily.util.BitSetHelpFunc;

import java.util.*;
import java.util.stream.Collectors;

public class IntSet implements Cloneable {
    private List<Integer> elements;

    public IntSet() {
        elements = new ArrayList<>();
    }

    public IntSet(BitSet bitSet) {
        elements = Arrays.stream(bitSet.stream().toArray()).boxed().collect(Collectors.toList());
    }

    public void add(int element) {
        elements.add(element);
    }

    /**
     * 一次性设置元素集合
     */
    public void set(Collection<Integer> elements) {
        this.elements = new ArrayList<>(elements);
    }

    /**
     * 返回含有的元素的集合
     */
    public List<Integer> get() {
        return elements;
    }

    /**
     * 返回指定序号的元素
     */
    public int get(int index) {
        return elements.get(index);
    }

    /**
     * 返回含有的元素的个数
     */
    public int size() {
        return elements.size();
    }

    public static BitSet transformIntSetToBitSet(IntSet intSet) {
        BitSet bitSet = new BitSet();
        for (int element : intSet.get()) {
            bitSet.set(element);
        }
        return bitSet;
    }

//    private static BitSet transformIntSetToBitSet(IntSet intSet, int size) {
//        BitSet bitSet = new BitSet(size);
//        for (int element : intSet.get()) {
//            bitSet.set(element);
//        }
//        return bitSet;
//    }

    /**
     * 不改变a和b
     */
    public static IntSet and(IntSet a, IntSet b) {
        BitSet bitSetOfA = transformIntSetToBitSet(a);
        bitSetOfA.and(transformIntSetToBitSet(b));
        return new IntSet(bitSetOfA);
    }

    /**
     * 不改变a和b
     */
    public static IntSet and(IntSet a, BitSet b) {
//        BitSet bitSetOfA = transformIntSetToBitSet(a);
//        bitSetOfA.and(b);
//        return new IntSet(bitSetOfA);
        IntSet res = new IntSet();
        for (int element : a.get()) {
            if (b.get(element)) res.add(element);
        }
        return res;
    }

    /**
     * 改变自身
     */
    public void and(IntSet b) {
        this.elements = IntSet.and(this, b).elements;
    }

    /**
     * 不改变a和b
     */
    public static IntSet or(IntSet a, IntSet b) {
        BitSet bitSetOfA = transformIntSetToBitSet(a);
        IntSet.or(bitSetOfA, b);
        return new IntSet(bitSetOfA);
    }

    /**
     * 改变a
     */
    public static void or(BitSet a, IntSet b) {
        for (int element : b.elements) {
            a.set(element);
        }
    }

    /**
     * 改变a
     */
    public void or(IntSet b) {
        this.elements = IntSet.or(this, b).elements;
    }

    /**
     * 不改变a和b
     */
    public static IntSet andNot(IntSet a, IntSet b) {
        BitSet bitSetOfA = transformIntSetToBitSet(a);
        IntSet.andNot(bitSetOfA, b);
        return new IntSet(bitSetOfA);
    }

    /**
     * 改变a
     */
    public static void andNot(BitSet a, IntSet b) {
        for (int element : b.elements) {
            a.clear(element);
        }
    }

    /**
     * 不改变a和b
     */
    public void andNot(IntSet b) {
        this.elements = IntSet.andNot(this, b).elements;
    }

    /**
     * 判断这个集合是不是集合b的子集
     */
    public boolean isSubsetOf(IntSet b) {
        //TODO:测试是用二分查找还是bitset快
        if (this.size() > b.size()) return false;
        List<Integer> elementsOfB = b.get();
        for (int element : elements) {
            int pos = Collections.binarySearch(elementsOfB, element);
            if (pos < 0) return false;
        }
//        BitSet bitSetOfB = IntSet.transformIntSetToBitSet(b);
//        for (int element : elements) {
//            if (!bitSetOfB.get(element)) return false;
//        }
        return true;
    }

    /**
     * 假设含两个元素a和b，返回b
     */
    public int another(int a){
        return (elements.get(0) == a) ? elements.get(1) : elements.get(0);
    }

    public boolean isEqual(BitSet b){
        if(this.size() != b.cardinality()) return false;
        return isSubsetOf(b);
    }

    public boolean isSubsetOf(BitSet b) {
        for (int element : elements) {
            if (!b.get(element)) return false;
        }
        return true;
    }

    /**
     * 判断a和b是否有共同的元素
     */
    public boolean intersects(BitSet b) {
        for (int element : elements) {
            if (b.get(element)) return true;
        }
        return false;
    }

    @Override
    public IntSet clone() throws CloneNotSupportedException {
        IntSet clone = (IntSet) super.clone();
        clone.set(new ArrayList<>(elements));
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        for (int element : elements) {
            if (stringBuilder.length() != 1) stringBuilder.append(",");
            stringBuilder.append(element);
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
