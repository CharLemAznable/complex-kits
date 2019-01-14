package com.github.charlemaznable.lang;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.lang.Condition.blankThen;
import static com.github.charlemaznable.lang.Condition.emptyThen;
import static com.github.charlemaznable.lang.Condition.nonBlank;
import static com.github.charlemaznable.lang.Condition.nonEmpty;
import static com.github.charlemaznable.lang.Condition.nonNull;
import static com.github.charlemaznable.lang.Condition.notBlankThen;
import static com.github.charlemaznable.lang.Condition.notEmptyThen;
import static com.github.charlemaznable.lang.Condition.notNullThen;
import static com.github.charlemaznable.lang.Condition.nullThen;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConditionTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testCondition() {
        String strnull = null;
        String strempty = "";
        String strblank = "  ";
        String string = "string";

        assertNull(nonNull(strnull));
        assertEquals(strempty, nonNull(strnull, strempty, strblank, string));

        assertNull(nonEmpty(strnull, strempty));
        assertEquals(strblank, nonEmpty(strnull, strempty, strblank, string));

        assertNull(nonBlank(strnull, strempty, strblank));
        assertEquals(string, nonBlank(strnull, strempty, strblank, string));

        assertNull(notNullThen(strnull, s -> "nonNull"));
        assertEquals("nonNull", notNullThen(strempty, s -> "nonNull"));

        assertNull(notEmptyThen(strempty, s -> "nonNull"));
        assertEquals("nonNull", notEmptyThen(strblank, s -> "nonNull"));

        assertNull(notBlankThen(strblank, s -> "nonNull"));
        assertEquals("nonNull", notBlankThen(string, s -> "nonNull"));

        assertEquals("nonNull", nullThen(strnull, () -> "nonNull"));
        assertEquals(strempty, nullThen(strempty, () -> "nonNull"));

        assertEquals("nonNull", emptyThen(strempty, () -> "nonNull"));
        assertEquals(strblank, emptyThen(strblank, () -> "nonNull"));

        assertEquals("nonNull", blankThen(strblank, () -> "nonNull"));
        assertEquals(string, blankThen(string, () -> "nonNull"));
    }
}
