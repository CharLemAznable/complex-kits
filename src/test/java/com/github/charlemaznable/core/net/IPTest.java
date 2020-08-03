package com.github.charlemaznable.core.net;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.net.IP.V4;
import static com.github.charlemaznable.core.net.IP.V6;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
public class IPTest {

    @Test
    public void testIPV4() {
        String ip = V4.netIP();
        log.info(ip);
        assertNotEquals("127.0.0.1", ip);
        ip = V4.localIP();
        log.info(ip);
        assertNotEquals("127.0.0.1", ip);
    }

    @Test
    public void testIPV6() {
        String ip = V6.netIP();
        log.info(ip);
        assertNotEquals("0:0:0:0:0:0:0:1", ip);
        ip = V6.localIP();
        log.info(ip);
        assertNotEquals("0:0:0:0:0:0:0:1", ip);
    }
}
