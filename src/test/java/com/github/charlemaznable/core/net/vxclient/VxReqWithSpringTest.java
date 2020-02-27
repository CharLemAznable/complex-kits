package com.github.charlemaznable.core.net.vxclient;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, VertxExtension.class})
@ContextConfiguration(classes = VxReqWithSpringConfiguration.class)
public class VxReqWithSpringTest extends VxReqCommonTest {

    @Autowired
    private Vertx vertx;

    @Test
    public void testVxReqWithSpring(VertxTestContext test) {
        testVxReq(vertx, test);
    }
}
