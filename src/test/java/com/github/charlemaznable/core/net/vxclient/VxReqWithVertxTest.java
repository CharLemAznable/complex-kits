package com.github.charlemaznable.core.net.vxclient;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class VxReqWithVertxTest extends VxReqCommonTest {

    @Test
    public void testVxReqWithVertx(Vertx vertx, VertxTestContext test) {
        testVxReq(vertx, test);
    }
}
