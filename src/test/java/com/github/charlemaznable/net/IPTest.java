package com.github.charlemaznable.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class IPTest {

    @Test
    public void testIP() {
        String ip = IP.netIP();
        assertNotEquals("127.0.0.1", ip);
    }
}
