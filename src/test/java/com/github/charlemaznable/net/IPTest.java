package com.github.charlemaznable.net;

import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.net.IP.netIP;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class IPTest {

    @Test
    public void testIP() {
        val ip = netIP();
        assertNotEquals("127.0.0.1", ip);
    }
}
