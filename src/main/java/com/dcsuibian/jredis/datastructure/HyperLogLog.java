package com.dcsuibian.jredis.datastructure;

import org.apache.commons.codec.digest.MurmurHash2;

public class HyperLogLog {
    private static final int P = 14; /* The greater is P, the smaller the error. */
    private static final int Q = 64 - P; /* The number of bits of the hash value used for determining the number of leading zeros. */
    private static final int REGISTERS = 1 << P; /* With P=14, 16384 registers. */
    private static final int P_MASK = REGISTERS - 1; /* Mask to index register. */
    private static final int BITS = 6; /* Enough to count up to 63 leading zeroes. */
    private static final int REGISTER_MAX = (1 << BITS) - 1;
    private static final double ALPHA_INFINITY = 0.721347520444481703680;

    private final byte[] registers;

    public HyperLogLog() {
        this.registers = new byte[(REGISTERS * BITS + 7) / 8 + 1];
    }

    private int getRegister(int index) {
        int byteIndex = index * BITS / 8;
        int fb = index * BITS & 7;
        int fb8 = 8 - fb;
        int b0 = registers[byteIndex];
        int b1 = registers[byteIndex + 1];
        return ((b0 >> fb) | (b1 << fb8)) & REGISTER_MAX;
    }

    private void setRegister(int index, int value) {
        int byteIndex = index * BITS / 8;
        int fb = index * BITS & 7;
        int fb8 = 8 - fb;
        registers[byteIndex] &= (byte) ~(REGISTER_MAX << fb);
        registers[byteIndex] |= (byte) (value << fb);
        registers[byteIndex + 1] &= (byte) ~(REGISTER_MAX >> fb8);
        registers[byteIndex + 1] |= (byte) (value >> fb8);
    }

    private int patternLength(byte[] element, IntContainer registerPointer) {
        long hash = MurmurHash2.hash64(element, element.length, 0xADC83B19);
        int index = (int) (hash & P_MASK);
        hash >>= P;
        hash |= 1L << Q;
        long bit = 1;
        int count = 1;
        while ((hash & bit) == 0) {
            count++;
            bit <<= 1;
        }
        registerPointer.setValue(index);
        return count;
    }

    private boolean set(int index, int count) {
        int oldCount = getRegister(index);
        if (count > oldCount) {
            setRegister(index, count);
            return true;
        } else {
            return false;
        }
    }

    private int[] registerHistogram() {
        int[] histogram = new int[64];
        for (int i = 0; i < REGISTERS; i++) {
            int count = getRegister(i);
            histogram[count]++;
        }
        return histogram;
    }

    private double sigma(double x) {
        if (1 == x) return Double.POSITIVE_INFINITY;
        double zPrime;
        double y = 1;
        double z = x;
        do {
            x *= x;
            zPrime = z;
            z += x * y;
            y += y;
        } while (zPrime != z);
        return z;
    }

    private double tau(double x) {
        if (0 == x || 1 == x) return 0;
        double zPrime;
        double y = 1;
        double z = 1 - x;
        do {
            x = Math.sqrt(x);
            zPrime = z;
            y *= 0.5;
            z -= Math.pow(1 - x, 2) * y;
        } while (zPrime != z);
        return z / 3;
    }

    public boolean add(byte[] element) {
        IntContainer registerPointer = new IntContainer();
        int count = patternLength(element, registerPointer);
        return set(registerPointer.getValue(), count);
    }

    public long count() {
        double m = REGISTERS;
        int[] histogram = registerHistogram();
        double z = m * tau((m - histogram[Q + 1]) / m);
        for (int i = Q; i >= 1; i--) {
            z += histogram[i];
            z *= 0.5;
        }
        z += m * sigma(histogram[0] / m);
        return Math.round(ALPHA_INFINITY * m * m / z);
    }

    public HyperLogLog merge(HyperLogLog another) {
        HyperLogLog result = new HyperLogLog();
        for (int i = 0; i < REGISTERS; i++) {
            int thisCount = getRegister(i);
            int anotherCount = another.getRegister(i);
            result.setRegister(i, Math.max(thisCount, anotherCount));
        }
        return result;
    }
}
