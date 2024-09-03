package com.dcsuibian.jredis.datastructure;

import lombok.Getter;
import lombok.Setter;

public class ZSet {
    private final Dictionary<Sds, Double> dict;
    private final ZSkipList zsl;

    public ZSet() {
        this.dict = new Dictionary<>();
        this.zsl = new ZSkipList();
    }
}

class ZSkipList {
    private static final int MAX_LEVEL = 32;
    private static final double P = 0.25;

    private static final class ZSkipListLevel {
        private ZSkipListNode forward;
        private long span;
    }

    private static final class ZSkipListNode {
        private Sds element;
        private double score;
        private ZSkipListNode backward;
        private ZSkipListLevel[] level;

        public ZSkipListNode(int level, double score, Sds element) {
            this.score = score;
            this.element = element;
            this.level = new ZSkipListLevel[level];
            for (int i = 0; i < level; i++) {
                this.level[i] = new ZSkipListLevel();
            }
        }
    }

    private ZSkipListNode head;
    private ZSkipListNode tail;
    private long length;
    private int level;

    public ZSkipList() {
        this.level = 1;
        this.length = 0;
        this.head = new ZSkipListNode(MAX_LEVEL, 0, null);
        for (int i = 0; i < MAX_LEVEL; i++) {
            this.head.level[i].forward = null;
            this.head.level[i].span = 0;
        }
        this.head.backward = null;
        this.tail = null;
    }

    private int randomLevel() {
        int level = 1;
        while (Math.random() < P && level < MAX_LEVEL) {
            level++;
        }
        return level;
    }

    private ZSkipListNode insert(double score, Sds element) {
        ZSkipListNode[] update = new ZSkipListNode[MAX_LEVEL];
        long[] rank = new long[MAX_LEVEL];
        ZSkipListNode x = this.head;
        for (int i = this.level - 1; i >= 0; i--) {
            rank[i] = i == this.level - 1 ? 0 : rank[i + 1];
            while (null != x.level[i].forward && (x.level[i].forward.score < score || (x.level[i].forward.score == score && x.level[i].forward.element.compareTo(element) < 0))) {
                rank[i] += x.level[i].span;
                x = x.level[i].forward;
            }
            update[i] = x;
        }
        int level = randomLevel();
        if (level > this.level) {
            for (int i = this.level; i < level; i++) {
                rank[i] = 0;
                update[i] = this.head;
                update[i].level[i].span = this.length;
            }
            this.level = level;
        }
        x = new ZSkipListNode(level, score, element);
        for (int i = 0; i < level; i++) {
            x.level[i].forward = update[i].level[i].forward;
            update[i].level[i].forward = x;
            x.level[i].span = update[i].level[i].span - (rank[0] - rank[i]);
            update[i].level[i].span = (rank[0] - rank[i]) + 1;
        }
        for (int i = level; i < this.level; i++) {
            update[i].level[i].span++;
        }
        x.backward = update[0] == this.head ? null : update[0];
        if (null != x.level[0].forward) {
            x.level[0].forward.backward = x;
        } else {
            this.tail = x;
        }
        this.length++;
        return x;
    }

    private void removeNode(ZSkipListNode x, ZSkipListNode[] update) {
        for (int i = 0; i < this.level; i++) {
            if (update[i].level[i].forward == x) {
                update[i].level[i].span += x.level[i].span - 1;
                update[i].level[i].forward = x.level[i].forward;
            } else {
                update[i].level[i].span--;
            }
        }
        if (null != x.level[0].forward) {
            x.level[0].forward.backward = x.backward;
        } else {
            this.tail = x.backward;
        }
        while (this.level > 1 && null == this.head.level[this.level - 1].forward) {
            this.level--;
        }
        this.length--;
    }

    private boolean remove(double score, Sds element, ZSkipListNode[] node) {
        ZSkipListNode[] update = new ZSkipListNode[MAX_LEVEL];
        ZSkipListNode x = this.head;
        for (int i = this.level - 1; i >= 0; i--) {
            while (null != x.level[i].forward && (x.level[i].forward.score < score || (x.level[i].forward.score == score && x.level[i].forward.element.compareTo(element) < 0))) {
                x = x.level[i].forward;
            }
            update[i] = x;
        }
        x = x.level[0].forward;
        if (null != x && x.score == score && x.element.compareTo(element) == 0) {
            removeNode(x, update);
            if (null == node) {
                x = null;
            } else {
                node[0] = x;
            }
            return true;
        }
        return false;
    }

    private ZSkipListNode updateScore(double oldScore, Sds element, double newScore) {
        ZSkipListNode[] update = new ZSkipListNode[MAX_LEVEL];
        ZSkipListNode x = this.head;
        for (int i = this.level - 1; i >= 0; i--) {
            while (null != x.level[i].forward && (x.level[i].forward.score < oldScore || (x.level[i].forward.score == oldScore && x.level[i].forward.element.compareTo(element) < 0))) {
                x = x.level[i].forward;
            }
            update[i] = x;
        }
        x = x.level[0].forward;
        if ((null == x.backward || x.backward.score < newScore) && (null == x.level[0].forward || x.level[0].forward.score > newScore)) {
            x.score = newScore;
            return x;
        }
        removeNode(x, update);
        ZSkipListNode newNode = insert(newScore, x.element);
        x.element = null;
        return newNode;
    }

    private boolean valueGreaterThanOrEqualToMin(double value, ZRangeSpecification spec) {
        return spec.isMinExclusive() ? value > spec.getMin() : value >= spec.getMin();
    }

    private boolean valueLessThanOrEqualToMax(double value, ZRangeSpecification spec) {
        return spec.isMaxExclusive() ? value < spec.getMax() : value <= spec.getMax();
    }

    private boolean isInRange(ZRangeSpecification range) {
        if (range.getMin() > range.getMax() || (range.getMin() == range.getMax() && (range.isMinExclusive() || range.isMaxExclusive()))) {
            return false;
        }
        ZSkipListNode x = this.tail;
        if (null == x || !valueGreaterThanOrEqualToMin(x.score, range)) {
            return false;
        }
        x = this.head.level[0].forward;
        return null != x && valueLessThanOrEqualToMax(x.score, range);
    }

    /**
     * Find the first node that is contained in the specified range.
     * Returns null when no element is contained in the range.
     */
    private ZSkipListNode firstInRange(ZRangeSpecification range) {
        if (!isInRange(range)) {
            return null;
        }
        ZSkipListNode x = this.head;
        for (int i = this.level - 1; i >= 0; i--) {
            while (null != x.level[i].forward && !valueGreaterThanOrEqualToMin(x.level[i].forward.score, range)) {
                x = x.level[i].forward;
            }
        }
        x = x.level[0].forward;
        if (!valueLessThanOrEqualToMax(x.score, range)) {
            return null;
        }
        return x;
    }

    /**
     * Find the last node that is contained in the specified range.
     * Returns null when no element is contained in the range.
     */
    private ZSkipListNode lastInRange(ZRangeSpecification range) {
        if (!isInRange(range)) {
            return null;
        }
        ZSkipListNode x = this.head;
        for (int i = this.level - 1; i >= 0; i--) {
            while (null != x.level[i].forward && valueLessThanOrEqualToMax(x.level[i].forward.score, range)) {
                x = x.level[i].forward;
            }
        }
        if (!valueGreaterThanOrEqualToMin(x.score, range)) {
            return null;
        }
        return x;
    }

    private long getRank(double score, Sds element) {
        ZSkipListNode x = this.head;
        long rank = 0;
        for (int i = this.level - 1; i >= 0; i--) {
            while (null != x.level[i].forward && (x.level[i].forward.score < score || (x.level[i].forward.score == score && x.level[i].forward.element.compareTo(element) <= 0))) {
                rank += x.level[i].span;
                x = x.level[i].forward;
            }
            if (null != x.element && x.score == score && x.element.compareTo(element) == 0) {
                return rank;
            }
        }
        return 0;
    }

    /**
     * Finds a node by its rank. The rank argument needs to be 1-based.
     */
    private ZSkipListNode getNodeByRank(long rank) {
        ZSkipListNode x = this.head;
        long traversed = 0;
        for (int i = this.level - 1; i >= 0; i--) {
            while (null != x.level[i].forward && traversed + x.level[i].span <= rank) {
                traversed += x.level[i].span;
                x = x.level[i].forward;
            }
            if (traversed == rank) {
                return x;
            }
        }
        return null;
    }

    private boolean lexicographicalOrderValueGreaterThanOrEqualToMin(Sds value, ZLexicographicalOrderRangeSpecification spec) {
        return spec.isMinExclusive() ? value.compareTo(spec.getMin()) > 0 : value.compareTo(spec.getMin()) >= 0;
    }

    private boolean lexicographicalOrderValueLessThanOrEqualToMax(Sds value, ZLexicographicalOrderRangeSpecification spec) {
        return spec.isMaxExclusive() ? value.compareTo(spec.getMax()) < 0 : value.compareTo(spec.getMax()) <= 0;
    }

    private boolean isInLexicographicalOrderRange(ZLexicographicalOrderRangeSpecification range) {
        int compareResult = range.getMin().compareTo(range.getMax());
        if (compareResult > 0 || (compareResult == 0 && (range.isMinExclusive() || range.isMaxExclusive()))) {
            return false;
        }
        ZSkipListNode x = this.tail;
        if (null == x || !lexicographicalOrderValueGreaterThanOrEqualToMin(x.element, range)) {
            return false;
        }
        x = this.head.level[0].forward;
        return null != x && lexicographicalOrderValueLessThanOrEqualToMax(x.element, range);
    }

    private ZSkipListNode firstInLexicographicalOrderRange(ZLexicographicalOrderRangeSpecification range) {
        if (!isInLexicographicalOrderRange(range)) {
            return null;
        }
        ZSkipListNode x = this.head;
        for (int i = this.level - 1; i >= 0; i--) {
            while (null != x.level[i].forward && !lexicographicalOrderValueGreaterThanOrEqualToMin(x.level[i].forward.element, range)) {
                x = x.level[i].forward;
            }
        }
        x = x.level[0].forward;
        if (!lexicographicalOrderValueLessThanOrEqualToMax(x.element, range)) {
            return null;
        }
        return x;
    }

    private ZSkipListNode lastInLexicographicalOrderRange(ZLexicographicalOrderRangeSpecification range) {
        if (!isInLexicographicalOrderRange(range)) {
            return null;
        }
        ZSkipListNode x = this.head;
        for (int i = this.level - 1; i >= 0; i--) {
            while (null != x.level[i].forward && lexicographicalOrderValueLessThanOrEqualToMax(x.level[i].forward.element, range)) {
                x = x.level[i].forward;
            }
        }
        if (!lexicographicalOrderValueGreaterThanOrEqualToMin(x.element, range)) {
            return null;
        }
        return x;
    }
}

@Getter
@Setter
class ZRangeSpecification {
    private double min;
    private double max;
    private boolean minExclusive;
    private boolean maxExclusive;
}

@Getter
@Setter
class ZLexicographicalOrderRangeSpecification {
    private Sds min;
    private Sds max;
    private boolean minExclusive;
    private boolean maxExclusive;
}
