package com.dcsuibian.jredis.datastructure;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Dictionary<K, V> {
    private static final byte DICTIONARY_INITIAL_SIZE_EXP = 2;
    private static final int DICTIONARY_INITIAL_SIZE = 1 << DICTIONARY_INITIAL_SIZE_EXP;

    public static byte nextExp(int size) {
        byte exp = DICTIONARY_INITIAL_SIZE_EXP;
        if (size > 0x4000_0000L) {
            return -1;
        }
        while (true) {
            if ((1L << exp) >= size) {
                return exp;
            }
            exp++;
        }
    }

    public static int dictionarySize(int exp) {
        return -1 == exp ? 0 : 1 << exp;
    }

    public static int dictionarySizeMask(int exp) {
        return -1 == exp ? 0 : dictionarySize(exp) - 1;
    }

    @Getter
    @Setter
    public static class Entry<K, V> {
        private final K key;
        private V value;
        private Entry<K, V> next;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final List<List<Entry<K, V>>> tables;
    private final int[] used;
    private int rehashIndex;
    private short pauseRehash;

    private final byte[] sizeExp;

    public Dictionary() {
        tables = new ArrayList<>(2);
        tables.add(new ArrayList<>(Collections.nCopies(DICTIONARY_INITIAL_SIZE, null)));
        tables.add(new ArrayList<>(Collections.nCopies(DICTIONARY_INITIAL_SIZE, null)));
        used = new int[2];
        sizeExp = new byte[2];
        tableReset(0);
        tableReset(1);
        rehashIndex = -1;
        pauseRehash = 0;
    }

    public boolean isRehashing() {
        return -1 != rehashIndex;
    }

    public boolean resize() {
        if (isRehashing()) {
            return false;
        }
        int minimal = used[0];
        if (minimal < DICTIONARY_INITIAL_SIZE) {
            minimal = DICTIONARY_INITIAL_SIZE;
        }
        return expand(minimal);
    }

    public boolean expand(int size) {
        /* the size is invalid if it is smaller than the number of
         * elements already inside the hash table */
        if (isRehashing() || used[0] > size) {
            return false;
        }
        List<Entry<K, V>> newTable;
        int newUsed;
        byte newSizeExp = nextExp(size);
        /* Detect overflows */
        if (newSizeExp < 0) {
            return false;
        }
        /* Rehashing to the same table size is not useful. */
        if (newSizeExp == sizeExp[0]) {
            return false;
        }
        int newSize = 1 << newSizeExp;
        newTable = new ArrayList<>(newSize);
        for (int i = 0; i < newSize; i++) {
            newTable.add(null);
        }
        newUsed = 0;
        /* Is this the first initialization? If so it's not really a rehashing
         * we just set the first hash table so that it can accept keys. */
        if (null == tables.get(0)) {
            sizeExp[0] = newSizeExp;
            used[0] = newUsed;
            tables.set(0, newTable);
            return true;
        }
        /* Prepare a second hash table for incremental rehashing */
        sizeExp[1] = newSizeExp;
        used[1] = newUsed;
        tables.set(1, newTable);
        rehashIndex = 0;
        return true;
    }

    /**
     * @return true if the rehashing was completed. false if there are still keys to move from the old to the new hash table.
     */
    public boolean rehash(int n) {
        int emptyVisits = n * 10; /* Max number of empty buckets to visit. */
        if (!isRehashing()) {
            return true;
        }
        while (n-- > 0 && used[0] != 0) {
            while (null == tables.get(0).get(rehashIndex)) {
                rehashIndex++;
                if (--emptyVisits == 0) {
                    return false;
                }
            }
            Entry<K, V> entry = tables.get(0).get(rehashIndex);
            while (null != entry) {
                Entry<K, V> nextEntry = entry.next;
                /* Get the index in the new hash table */
                int hash = entry.key.hashCode() & ((1 << sizeExp[1]) - 1);
                entry.next = tables.get(1).get(hash);
                tables.get(1).set(hash, entry);
                used[0]--;
                used[1]++;
                entry = nextEntry;
            }
            tables.get(0).set(rehashIndex, null);
            rehashIndex++;
        }
        /* Check if we already rehashed the whole table... */
        if (0 == used[0]) {
            tables.set(0, tables.get(1));
            used[0] = used[1];
            sizeExp[0] = sizeExp[1];
            tableReset(1);
            rehashIndex = -1;
            return true;
        }

        /* More to rehash... */
        return false;
    }

    public V remove(K key) {
        /* dict is empty */
        if (0 == size()) {
            return null;
        }
        if (isRehashing()) {
            rehashStep();
        }
        int hash = key.hashCode();
        for (int tableIndex = 0; tableIndex <= 1; tableIndex++) {
            int index = hash & dictionarySizeMask(sizeExp[tableIndex]);
            Entry<K, V> entry = tables.get(tableIndex).get(index);
            Entry<K, V> prevEntry = null;
            while (null != entry) {
                if (entry.key.equals(key)) {
                    if (null != prevEntry) {
                        prevEntry.next = entry.next;
                    } else {
                        tables.get(tableIndex).set(index, entry.next);
                    }
                    used[tableIndex]--;
                    return entry.value;
                }
                prevEntry = entry;
                entry = entry.next;
            }
            if (!isRehashing()) {
                break;
            }
        }
        return null;
    }

    public int size() {
        return used[0] + used[1];
    }

    public V get(K key) {
        if (isRehashing()) {
            rehashStep();
        }
        int hash = key.hashCode();
        for (int tableIndex = 0; tableIndex <= 1; tableIndex++) {
            int index = hash & dictionarySizeMask(sizeExp[tableIndex]);
            Entry<K, V> entry = tables.get(tableIndex).get(index);
            while (null != entry) {
                if (entry.key.equals(key)) {
                    return entry.value;
                }
                entry = entry.next;
            }
            if (!isRehashing()) {
                break;
            }
        }
        return null;
    }

    public V put(K key, V value) {
        if (isRehashing()) {
            rehashStep();
        }
        int hash = key.hashCode();
        for (int tableIndex = 0; tableIndex <= 1; tableIndex++) {
            int index = hash & dictionarySizeMask(sizeExp[tableIndex]);
            Entry<K, V> entry = tables.get(tableIndex).get(index);
            while (null != entry) {
                if (entry.key.equals(key)) {
                    V oldValue = entry.value;
                    entry.value = value;
                    return oldValue;
                }
                entry = entry.next;
            }
            if (!isRehashing()) {
                break;
            }
        }
        Entry<K, V> entry = new Entry<>(key, value);
        int tableIndex = isRehashing() ? 1 : 0;
        entry.next = tables.get(tableIndex).get(hash & dictionarySizeMask(sizeExp[tableIndex]));
        tables.get(tableIndex).set(hash & dictionarySizeMask(sizeExp[tableIndex]), entry);
        used[tableIndex]++;
        return null;
    }

    public Set<K> keySet() {
        Set<K> keySet = new LinkedHashSet<>();
        for (int tableIndex = 0; tableIndex <= 1; tableIndex++) {
            for (Entry<K, V> entry : tables.get(tableIndex)) {
                while (null != entry) {
                    keySet.add(entry.key);
                    entry = entry.next;
                }
            }
        }
        return keySet;
    }

    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = new LinkedHashSet<>();
        for (int tableIndex = 0; tableIndex <= 1; tableIndex++) {
            for (Entry<K, V> entry : tables.get(tableIndex)) {
                while (null != entry) {
                    entrySet.add(entry);
                    entry = entry.next;
                }
            }
        }
        return entrySet;
    }

    public void clear() {
        tableReset(0);
        tableReset(1);
        rehashIndex = -1;
        pauseRehash = 0;
    }

    private void tableReset(int index) {
        tables.set(index, new ArrayList<>(Collections.nCopies(dictionarySize(sizeExp[index]), null)));
        sizeExp[index] = -1;
        used[index] = 0;
    }

    private void rehashStep() {
        if (0 == pauseRehash) {
            rehash(1);
        }
    }
}
