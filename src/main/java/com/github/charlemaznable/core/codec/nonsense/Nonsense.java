package com.github.charlemaznable.core.codec.nonsense;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.SecureRandom;

import static org.apache.commons.lang3.RandomStringUtils.random;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class Nonsense {

    private static final SecureRandom RANDOM = new SecureRandom();

    private String key;
    private String value;

    public static Nonsense nonsense() {
        return nonsense(new NonsenseOptions());
    }

    public static Nonsense nonsense(int count) {
        return nonsense(new NonsenseOptions().count(count));
    }

    public static Nonsense nonsense(String key) {
        return nonsense(new NonsenseOptions().key(key));
    }

    public static Nonsense nonsense(int count, String key) {
        return nonsense(new NonsenseOptions().key(key).count(count));
    }

    public static Nonsense nonsenseAscii() {
        return nonsense(new NonsenseOptions()
                .start(32).end(127).letters(false).numbers(false));
    }

    public static Nonsense nonsenseAscii(int count) {
        return nonsense(new NonsenseOptions().count(count)
                .start(32).end(127).letters(false).numbers(false));
    }

    public static Nonsense nonsenseAscii(String key) {
        return nonsense(new NonsenseOptions().key(key)
                .start(32).end(127).letters(false).numbers(false));
    }

    public static Nonsense nonsenseAscii(int count, String key) {
        return nonsense(new NonsenseOptions().key(key).count(count)
                .start(32).end(127).letters(false).numbers(false));
    }

    public static Nonsense nonsenseNumbers() {
        return nonsense(new NonsenseOptions().letters(false));
    }

    public static Nonsense nonsenseNumbers(int count) {
        return nonsense(new NonsenseOptions().count(count).letters(false));
    }

    public static Nonsense nonsenseNumbers(String key) {
        return nonsense(new NonsenseOptions().key(key).letters(false));
    }

    public static Nonsense nonsenseNumbers(int count, String key) {
        return nonsense(new NonsenseOptions().key(key).count(count).letters(false));
    }

    public static Nonsense nonsenseLetters() {
        return nonsense(new NonsenseOptions().numbers(false));
    }

    public static Nonsense nonsenseLetters(int count) {
        return nonsense(new NonsenseOptions().count(count).numbers(false));
    }

    public static Nonsense nonsenseLetters(String key) {
        return nonsense(new NonsenseOptions().key(key).numbers(false));
    }

    public static Nonsense nonsenseLetters(int count, String key) {
        return nonsense(new NonsenseOptions().key(key).count(count).numbers(false));
    }

    public static Nonsense nonsense(NonsenseOptions options) {
        return new Nonsense(options.key(), random(
                options.count(), options.start(), options.end(),
                options.letters(), options.numbers(), options.chars(), RANDOM));
    }
}
