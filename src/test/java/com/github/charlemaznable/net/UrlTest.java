package com.github.charlemaznable.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlTest {

    @Test
    public void testUrl() {
        new Url();

        assertEquals("%E6%B1%89%E5%AD%97", Url.encode("汉字"));
        assertEquals("汉字", Url.decode("%E6%B1%89%E5%AD%97"));

        assertEquals("abc_def---", Url.encodeDotAndColon("abc:def..."));
        assertEquals("abc:def...", Url.decodeDotAndColon("abc_def---"));
    }
}
