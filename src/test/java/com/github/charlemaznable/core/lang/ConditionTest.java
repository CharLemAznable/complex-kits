package com.github.charlemaznable.core.lang;

import com.github.charlemaznable.core.lang.ex.BadConditionException;
import com.github.charlemaznable.core.lang.ex.BlankStringException;
import com.github.charlemaznable.core.lang.ex.EmptyObjectException;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkCondition;
import static com.github.charlemaznable.core.lang.Condition.checkNotBlank;
import static com.github.charlemaznable.core.lang.Condition.checkNotEmpty;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.emptyThen;
import static com.github.charlemaznable.core.lang.Condition.nonBlank;
import static com.github.charlemaznable.core.lang.Condition.nonEmpty;
import static com.github.charlemaznable.core.lang.Condition.nonEquals;
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

    @Test
    public void testNonEquals() {
        assertEquals((short) 10, nonEquals((short) 0, (short) 10, (short) 20));
        assertEquals((short) 0, nonEquals((short) 0, (short) 0, (short) 0));
        assertEquals(10, nonEquals(0, 10, 20));
        assertEquals(0, nonEquals(0, 0, 0));
        assertEquals(10L, nonEquals(0L, 10L, 20L));
        assertEquals(0L, nonEquals(0L, 0L, 0L));
        assertEquals(10F, nonEquals(0F, 10F, 20F));
        assertEquals(0F, nonEquals(0F, 0F, 0F));
        assertEquals(10D, nonEquals(0D, 10D, 20D));
        assertEquals(0D, nonEquals(0D, 0D, 0D));
        assertEquals((byte) 10, nonEquals((byte) 0, (byte) 10, (byte) 20));
        assertEquals((byte) 0, nonEquals((byte) 0, (byte) 0, (byte) 0));
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

        assertThrows(BadConditionException.class, () -> checkCondition(() -> Objects.nonNull(strnull)));
        assertThrows(BadConditionException.class, () -> checkCondition(() -> Objects.nonNull(strnull), "strnull is Null"));
        assertThrows(ConditionTestException.class, () -> checkCondition(() -> Objects.nonNull(strnull), new ConditionTestException()));

        assertDoesNotThrow(() -> checkCondition(() -> Objects.isNull(strnull)));
        assertDoesNotThrow(() -> checkCondition(() -> Objects.isNull(strnull), "strnull is Null"));
        assertDoesNotThrow(() -> checkCondition(() -> Objects.isNull(strnull), new ConditionTestException()));

        val testBean = new ConditionTestBean();
        assertThrows(BadConditionException.class, () -> checkCondition(() -> Objects.nonNull(strnull), () -> testBean.setValue("true")));
        assertNull(testBean.getValue());
        assertThrows(BadConditionException.class, () -> checkCondition(() -> Objects.nonNull(strnull), () -> testBean.setValue("true"), "strnull is Null"));
        assertNull(testBean.getValue());
        assertThrows(ConditionTestException.class, () -> checkCondition(() -> Objects.nonNull(strnull), () -> testBean.setValue("true"), new ConditionTestException()));
        assertNull(testBean.getValue());

        assertDoesNotThrow(() -> checkCondition(() -> Objects.isNull(strnull), () -> testBean.setValue("1")));
        assertEquals("1", testBean.getValue());
        assertDoesNotThrow(() -> checkCondition(() -> Objects.isNull(strnull), () -> testBean.setValue("2"), "strnull is Null"));
        assertEquals("2", testBean.getValue());
        assertDoesNotThrow(() -> checkCondition(() -> Objects.isNull(strnull), () -> testBean.setValue("3"), new ConditionTestException()));
        assertEquals("3", testBean.getValue());

        assertThrows(BadConditionException.class, () -> checkCondition(() -> Objects.nonNull(strnull), () -> "result"));
        assertThrows(BadConditionException.class, () -> checkCondition(() -> Objects.nonNull(strnull), () -> "result", "strnull is Null"));
        assertThrows(ConditionTestException.class, () -> checkCondition(() -> Objects.nonNull(strnull), () -> "result", new ConditionTestException()));

        assertEquals("result", checkCondition(() -> Objects.isNull(strnull), () -> "result"));
        assertEquals("result", checkCondition(() -> Objects.isNull(strnull), () -> "result", "strnull is Null"));
        assertEquals("result", checkCondition(() -> Objects.isNull(strnull), () -> "result", new ConditionTestException()));
    }

    static class ConditionTestException extends RuntimeException {

        private static final long serialVersionUID = -4697342496228582709L;
    }

    @Data
    static class ConditionTestBean {

        private String value;
    }
}
