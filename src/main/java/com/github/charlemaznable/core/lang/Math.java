package com.github.charlemaznable.core.lang;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

public final class Math {

    private Math() {}

    public static int gcd(int p1, int p2) {
        int a1 = abs(p1);
        int a2 = abs(p2);
        int m1 = max(a1, a2);
        int m2 = min(a1, a2);
        while (m2 != 0) {
            int result = m1 % m2;
            m1 = m2;
            m2 = result;
        }
        return m1;
    }

    public static int gcd(int p1, int p2, int... params) {
        int result = gcd(p1, p2);
        for (int p : params) {
            result = gcd(result, p);
        }
        return result;
    }

    public static long gcd(long p1, long p2) {
        long a1 = abs(p1);
        long a2 = abs(p2);
        long m1 = max(a1, a2);
        long m2 = min(a1, a2);
        while (m2 != 0) {
            long result = m1 % m2;
            m1 = m2;
            m2 = result;
        }
        return m1;
    }

    public static long gcd(long p1, long p2, long... params) {
        long result = gcd(p1, p2);
        for (long p : params) {
            result = gcd(result, p);
        }
        return result;
    }
}
