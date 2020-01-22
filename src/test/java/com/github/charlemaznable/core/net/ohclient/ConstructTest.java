package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.internal.OhConstant;
import com.github.charlemaznable.core.net.ohclient.internal.ResponseBodyExtractor;
import org.joor.ReflectException;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConstructTest {

    @Test
    public void testOhConstant() {
        assertThrows(ReflectException.class, () ->
                onClass(OhConstant.class).create().get());
    }

    @Test
    public void testOhProxy$Elf() {
        assertThrows(ReflectException.class, () ->
                onClass("com.github.charlemaznable.core.net.ohclient.internal.OhProxy$Elf").create().get());
    }

    @Test
    public void testOhMappingProxy$Elf() {
        assertThrows(ReflectException.class, () ->
                onClass("com.github.charlemaznable.core.net.ohclient.internal.OhMappingProxy$Elf").create().get());
    }

    @Test
    public void testResponseBodyExtractor() {
        assertThrows(ReflectException.class, () ->
                onClass(ResponseBodyExtractor.class).create().get());
    }
}
