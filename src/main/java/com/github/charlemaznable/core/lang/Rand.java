package com.github.charlemaznable.core.lang;

import com.github.charlemaznable.core.lang.joou.ULong;
import lombok.val;
import lombok.var;

import java.security.SecureRandom;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Empty.isEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Math.max;
import static org.apache.commons.lang3.RandomStringUtils.random;

public class Rand {

    /*
     * Thread-safe. It uses synchronization to protect the integrity of its state.
     * See SecureRandom.nextBytes with synchronized keyword.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    private Rand() {}

    public static boolean randBoolean() {
        return RANDOM.nextBoolean();
    }

    public static double randDouble() {
        return RANDOM.nextDouble();
    }

    public static float randFloat() {
        return RANDOM.nextFloat();
    }

    public static int randInt() {
        return RANDOM.nextInt();
    }

    public static int randInt(int n) {
        return RANDOM.nextInt(n);
    }

    public static long randLong() {
        return RANDOM.nextLong();
    }

    public static String randNum(int count) {
        val sb = new StringBuilder(count);
        while (sb.length() < count) {
            sb.append(new ULong(randLong()));
        }

        return sb.replace(count, sb.length(), "").toString();
    }

    public static String randAscii(int count) {
        return random(count, 32, 127, false, false, null, RANDOM);
    }

    public static String randLetters(int count) {
        return random(count, 0, 0, true, false, null, RANDOM);
    }

    public static String randAlphanumeric(int count) {
        return random(count, 0, 0, true, true, null, RANDOM);
    }

    public static <T> T randWeighted(Map<T, Integer> weightedMap) {
        if (isEmpty(weightedMap)) return null;

        var sum = 1;
        for (val value : weightedMap.values()) sum += value;
        var rand = randInt(sum);

        for (val entry : weightedMap.entrySet()) {
            rand -= entry.getValue();
            if (rand <= 0) return entry.getKey();
        }
        return null;
    }

    public static <T> T randInverseWeighted(Map<T, Integer> weightedMap) {
        if (isEmpty(weightedMap)) return null;

        var sum = 0;
        for (val value : weightedMap.values()) sum += value;

        Map<T, Integer> inverseWeightedMap = newHashMap();
        for (val entry : weightedMap.entrySet()) {
            inverseWeightedMap.put(entry.getKey(),
                    (int) (10. * sum / max(0.1, entry.getValue())));
        }

        return randWeighted(inverseWeightedMap);
    }
}
