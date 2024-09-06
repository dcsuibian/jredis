package com.dcsuibian.jredis.util;

import com.dcsuibian.jredis.datastructure.BytePointer;

import java.nio.charset.StandardCharsets;

public class GenericUtil {
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

    /**
     * Glob-style pattern matching.
     */
    private static boolean match(BytePointer pattern, int patternLength, BytePointer string, int stringLength, boolean ignoreCase) {
        while (0 != patternLength && 0 != stringLength) {
            switch (pattern.get(0)) {
                case '*':
                    while (0 != patternLength && pattern.get(1) == '*') {
                        pattern = new BytePointer(pattern.getData(), pattern.getOffset() + 1);
                        patternLength--;
                    }
                    if (1 == patternLength) {
                        return true;
                    }
                    while (0 != stringLength) {
                        if (match(new BytePointer(pattern.getData(), pattern.getOffset() + 1), patternLength - 1, string, stringLength, ignoreCase)) {
                            return true;
                        }
                        string = new BytePointer(string.getData(), string.getOffset() + 1);
                        stringLength--;
                    }
                    return false;
                case '?':
                    string = new BytePointer(string.getData(), string.getOffset() + 1);
                    stringLength--;
                    break;
                case '[':
                    pattern = new BytePointer(pattern.getData(), pattern.getOffset() + 1);
                    patternLength--;
                    boolean not = pattern.get(0) == '^';
                    if (not) {
                        pattern = new BytePointer(pattern.getData(), pattern.getOffset() + 1);
                        patternLength--;
                    }
                    boolean match = false;
                    while (true) {
                        if (pattern.get(0) == '\\' && patternLength >= 2) {
                            pattern = new BytePointer(pattern.getData(), pattern.getOffset() + 1);
                            patternLength--;
                            if (pattern.get(0) == string.get(0)) {
                                match = true;
                            }
                        } else if (pattern.get(0) == ']') {
                            break;
                        } else if (0 == patternLength) {
                            pattern = new BytePointer(pattern.getData(), pattern.getOffset() - 1);
                            patternLength++;
                            break;
                        } else if (patternLength >= 3 && pattern.get(1) == '-') {
                            byte begin = pattern.get(0);
                            byte end = pattern.get(2);
                            byte c = string.get(0);
                            if (begin > end) {
                                byte t = begin;
                                begin = end;
                                end = t;
                            }
                            if (ignoreCase) {
                                begin = toLowerCase(begin);
                                end = toLowerCase(end);
                                c = toLowerCase(c);
                            }
                            pattern = new BytePointer(pattern.getData(), pattern.getOffset() + 2);
                            patternLength -= 2;
                            if (c >= begin && c <= end) {
                                match = true;
                            }
                        } else {
                            if (!ignoreCase) {
                                if (pattern.get(0) == string.get(0)) {
                                    match = true;
                                }
                            } else {
                                if (toLowerCase(pattern.get(0)) == toLowerCase(string.get(0))) {
                                    match = true;
                                }
                            }
                        }
                        pattern = new BytePointer(pattern.getData(), pattern.getOffset() + 1);
                        patternLength--;
                    }
                    if (not) {
                        match = !match;
                    }
                    if (!match) {
                        return false;
                    }
                    string = new BytePointer(string.getData(), string.getOffset() + 1);
                    stringLength--;
                    break;
                case '\\':
                    if (patternLength >= 2) {
                        pattern = new BytePointer(pattern.getData(), pattern.getOffset() + 1);
                        patternLength--;
                    }
                default:
                    if (!ignoreCase) {
                        if (pattern.get(0) != string.get(0)) {
                            return false;
                        }
                    } else {
                        if (toLowerCase(pattern.get(0)) != toLowerCase(string.get(0))) {
                            return false;
                        }
                    }
                    string = new BytePointer(string.getData(), string.getOffset() + 1);
                    stringLength--;
                    break;
            }
            pattern = new BytePointer(pattern.getData(), pattern.getOffset() + 1);
            patternLength--;
            if (0 == stringLength) {
                while ('*' == pattern.get(0)) {
                    pattern = new BytePointer(pattern.getData(), pattern.getOffset() + 1);
                    patternLength--;
                }
                break;
            }
        }
        return 0 == patternLength && 0 == stringLength;
    }

    public static boolean match(byte[] pattern, byte[] string, boolean ignoreCase) {
        byte[] patternCopy = new byte[pattern.length + 1];
        System.arraycopy(pattern, 0, patternCopy, 0, pattern.length);
        patternCopy[pattern.length] = '\0';
        byte[] stringCopy = new byte[string.length + 1];
        System.arraycopy(string, 0, stringCopy, 0, string.length);
        stringCopy[string.length] = '\0';
        return match(new BytePointer(patternCopy), pattern.length, new BytePointer(stringCopy), string.length, ignoreCase);
    }
}
