package com.github.charlemaznable.net;

import lombok.var;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.net.IP.localIP;
import static com.github.charlemaznable.net.IP.netIP;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class IPTest {

    @Test
    public void testIP() {
        var ip = netIP();
        assertNotEquals("127.0.0.1", ip);
        ip = localIP();
        assertNotEquals("127.0.0.1", ip);
    }
}
