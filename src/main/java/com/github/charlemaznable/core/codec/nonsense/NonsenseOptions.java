package com.github.charlemaznable.core.codec.nonsense;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Getter
@Setter
@Accessors(fluent = true)
public class NonsenseOptions {

    private static final String DEFAULT_KEY = "nonsense";
    private static final int DEFAULT_COUNT = 16;
    private static final int DEFAULT_START = 0;
    private static final int DEFAULT_END = 0;
    private static final boolean DEFAULT_LETTERS_ENABLED = true;
    private static final boolean DEFAULT_NUMBERS_ENABLED = true;
    private static final char[] DEFAULT_CHARS = null;

    private String key = DEFAULT_KEY;
    private int count = DEFAULT_COUNT;
    private int start = DEFAULT_START;
    private int end = DEFAULT_END;
    private boolean letters = DEFAULT_LETTERS_ENABLED;
    private boolean numbers = DEFAULT_NUMBERS_ENABLED;
    private char[] chars = DEFAULT_CHARS;
}
