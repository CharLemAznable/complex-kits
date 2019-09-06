package com.github.charlemaznable.core.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeeTest {

    @Test
    public void testTypee() {
        assertEquals(String.class, Typee.getActualTypeArgument(TestActual.class, TestInterface.class));
        assertEquals(String.class, Typee.getActualTypeArgument(TestActualSub.class, TestInterface.class));

        assertEquals(String.class, Typee.getActualTypeArgument(TestActualInterfaceSub.class, TestInterface.class));
        assertEquals(String.class, Typee.getActualTypeArgument(TestActualInterfaceSubSub.class, TestInterface.class));

        assertEquals(String.class, Typee.getActualTypeArgument(TestSub.class, TestBase.class));
        assertEquals(String.class, Typee.getActualTypeArgument(TestSub.class, TestInterface.class));
        assertEquals(String.class, Typee.getActualTypeArgument(TestSubSub.class, TestBase.class));
        assertEquals(String.class, Typee.getActualTypeArgument(TestSubSub.class, TestInterface.class));

        assertEquals(String.class, Typee.getActualTypeArgument(TestMiddleSub.class, TestMiddleInterface.class));
        assertEquals(String.class, Typee.getActualTypeArgument(TestMiddleSub.class, TestInterface.class));
        assertEquals(String.class, Typee.getActualTypeArgument(TestMiddleSubSub.class, TestMiddleInterface.class));
        assertEquals(String.class, Typee.getActualTypeArgument(TestMiddleSubSub.class, TestInterface.class));
    }

    interface TestInterface<T> {

        String getName(T instance);
    }

    interface TestActualInterface extends TestInterface<String> {}

    interface TestMiddleInterface<T> extends TestInterface<T> {}

    static class TestActual implements TestInterface<String> {

        @Override
        public String getName(String instance) {
            return "\"" + instance + "\'";
        }
    }

    static class TestActualSub extends TestActual {}

    static class TestActualInterfaceSub implements TestActualInterface {

        @Override
        public String getName(String instance) {
            return "\"" + instance + "\'";
        }
    }

    static class TestActualInterfaceSubSub extends TestActualInterfaceSub {}

    static abstract class TestBase<T> implements TestInterface<T> {}

    static class TestSub extends TestBase<String> {

        @Override
        public String getName(String instance) {
            return "\"" + instance + "\'";
        }
    }

    static class TestSubSub extends TestSub {}

    static class TestMiddleSub implements TestMiddleInterface<String> {

        @Override
        public String getName(String instance) {
            return "\"" + instance + "\'";
        }
    }

    static class TestMiddleSubSub extends TestMiddleSub {}
}
