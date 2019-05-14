package com.github.charlemaznable.net;

import lombok.var;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.net.IP.V4;
import static com.github.charlemaznable.net.IP.V6;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class IPTest {

    @Test
    public void testIPV4() {
        var ip = V4.netIP();
        System.out.println(ip);
        assertNotEquals("127.0.0.1", ip);
        ip = V4.localIP();
        System.out.println(ip);
        assertNotEquals("127.0.0.1", ip);
    }

    @Test
    public void testIPV6() {
        var ip = V6.netIP();
        System.out.println(ip);
        assertNotEquals("0:0:0:0:0:0:0:1", ip);
        ip = V6.localIP();
        System.out.println(ip);
        assertNotEquals("0:0:0:0:0:0:0:1", ip);
    }
}
