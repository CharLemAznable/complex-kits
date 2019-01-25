package com.github.charlemaznable.lang;

import com.github.charlemaznable.lang.ex.BlankStringException;
import com.github.charlemaznable.lang.ex.EmptyObjectException;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.lang.Condition.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConditionTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testCondition() {
        new Condition();

        String strnull = null;
        val strempty = "";
        val strblank = "  ";
        val string = "string";

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

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testConditionCheck() {
        String strnull = null;
        val strempty = "";
        val strblank = "  ";
        val string = "string";

        assertThrows(NullPointerException.class, () -> checkNotNull(strnull));
        assertThrows(NullPointerException.class, () -> checkNotNull(strnull, "strnull is Null"));
        assertThrows(ConditionTestException.class, () -> checkNotNull(strnull, new ConditionTestException()));

        assertEquals(strempty, checkNotNull(strempty));
        assertEquals(strempty, checkNotNull(strempty, "strempty is Null"));
        assertEquals(strempty, checkNotNull(strempty, new ConditionTestException()));

        assertThrows(EmptyObjectException.class, () -> checkNotEmpty(strempty));
        assertThrows(EmptyObjectException.class, () -> checkNotEmpty(strempty, "strempty is Empty"));
        assertThrows(ConditionTestException.class, () -> checkNotEmpty(strempty, new ConditionTestException()));

        assertEquals(strblank, checkNotEmpty(strblank));
        assertEquals(strblank, checkNotEmpty(strblank, "strblank is Empty"));
        assertEquals(strblank, checkNotEmpty(strblank, new ConditionTestException()));

        assertThrows(BlankStringException.class, () -> checkNotBlank(strblank));
        assertThrows(BlankStringException.class, () -> checkNotBlank(strblank, "strblank is Blank"));
        assertThrows(ConditionTestException.class, () -> checkNotBlank(strblank, new ConditionTestException()));

        assertEquals(string, checkNotBlank(string));
        assertEquals(string, checkNotBlank(string, "string is Blank"));
        assertEquals(string, checkNotBlank(string, new ConditionTestException()));
    }

    public static class ConditionTestException extends RuntimeException {

        private static final long serialVersionUID = -4697342496228582709L;
    }
}
