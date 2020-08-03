package com.github.charlemaznable.core.lang;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.Closeable;

import static com.github.charlemaznable.core.lang.Closer.closeQuietly;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CloserTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testCloser() {
        Object obj1 = null;
        val obj2 = new TestCloseable();
        val obj3 = new TestCloser();

        assertDoesNotThrow((Executable)
                () -> closeQuietly(obj1, obj2, obj2, obj3));
        assertTrue(obj2.isClosed());
        assertTrue(obj3.isClosed());
    }

    @Getter
    static class TestCloseable implements Closeable {

        private boolean closed;

        @Override
        public void close() {
            if (closed) throw new RuntimeException();
            closed = true;
        }
    }

    @Getter
    static class TestCloser {

        private boolean closed;

        public void close() {
            closed = true;
        }
    }
}
