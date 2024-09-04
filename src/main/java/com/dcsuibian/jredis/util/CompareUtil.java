package com.dcsuibian.jredis.util;

import java.nio.charset.StandardCharsets;

public class CompareUtil {
    private static byte toLowerCase(byte b) {
        if (b >= 'A' && b <= 'Z') {
            return (byte) (b + 32);
        } else {
            return b;
        }
    }

    public static boolean equalsIgnoreCase(byte[] s1, byte[] s2) {
        if (s1.length != s2.length) {
            return false;
        }
        for (int i = 0; i < s1.length; i++) {
            if (toLowerCase(s1[i]) != toLowerCase(s2[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean equalsIgnoreCase(byte[] s1, String s2) {
        return equalsIgnoreCase(s1, s2.getBytes(StandardCharsets.UTF_8));
    }
}
