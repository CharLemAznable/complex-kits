package com.github.charlemaznable.core.lang;

import com.github.charlemaznable.core.lang.ex.BlankStringException;
import com.github.charlemaznable.core.lang.ex.EmptyObjectException;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkCondition;
import static com.github.charlemaznable.core.lang.Condition.checkNotBlank;
import static com.github.charlemaznable.core.lang.Condition.checkNotEmpty;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.nonBlank;
import static com.github.charlemaznable.core.lang.Condition.nonEmpty;
import static com.github.charlemaznable.core.lang.Condition.nonNull;
import static com.github.charlemaznable.core.lang.Condition.notBlankThen;
import static com.github.charlemaznable.core.lang.Condition.notEmptyThen;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testConditionCheckCondition() {
        String strnull = null;

        assertThrows(RuntimeException.class, () -> checkCondition(() -> null != strnull));
        assertThrows(RuntimeException.class, () -> checkCondition(() -> null != strnull, "strnull is Null"));
        assertThrows(ConditionTestException.class, () -> checkCondition(() -> null != strnull, new ConditionTestException()));

        assertDoesNotThrow(() -> checkCondition(() -> null == strnull));
        assertDoesNotThrow(() -> checkCondition(() -> null == strnull, "strnull is Null"));
        assertDoesNotThrow(() -> checkCondition(() -> null == strnull, new ConditionTestException()));

        val testBean = new ConditionTestBean();
        assertThrows(RuntimeException.class, () -> checkCondition(() -> null != strnull, () -> testBean.setValue("true")));
        assertNull(testBean.getValue());
        assertThrows(RuntimeException.class, () -> checkCondition(() -> null != strnull, () -> testBean.setValue("true"), "strnull is Null"));
        assertNull(testBean.getValue());
        assertThrows(ConditionTestException.class, () -> checkCondition(() -> null != strnull, () -> testBean.setValue("true"), new ConditionTestException()));
        assertNull(testBean.getValue());

        assertDoesNotThrow(() -> checkCondition(() -> null == strnull, () -> testBean.setValue("1")));
        assertEquals("1", testBean.getValue());
        assertDoesNotThrow(() -> checkCondition(() -> null == strnull, () -> testBean.setValue("2"), "strnull is Null"));
        assertEquals("2", testBean.getValue());
        assertDoesNotThrow(() -> checkCondition(() -> null == strnull, () -> testBean.setValue("3"), new ConditionTestException()));
        assertEquals("3", testBean.getValue());

        assertThrows(RuntimeException.class, () -> checkCondition(() -> null != strnull, () -> "result"));
        assertThrows(RuntimeException.class, () -> checkCondition(() -> null != strnull, () -> "result", "strnull is Null"));
        assertThrows(ConditionTestException.class, () -> checkCondition(() -> null != strnull, () -> "result", new ConditionTestException()));

        assertEquals("result", checkCondition(() -> null == strnull, () -> "result"));
        assertEquals("result", checkCondition(() -> null == strnull, () -> "result", "strnull is Null"));
        assertEquals("result", checkCondition(() -> null == strnull, () -> "result", new ConditionTestException()));
    }

    static class ConditionTestException extends RuntimeException {

        private static final long serialVersionUID = -4697342496228582709L;
    }

    @Data
    static class ConditionTestBean {

        private String value;
    }
}