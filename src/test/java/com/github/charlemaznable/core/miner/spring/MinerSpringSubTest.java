package com.github.charlemaznable.core.miner.spring;

import com.github.charlemaznable.core.miner.testminer.TestMiner;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MinerSpringSubConfiguration.class)
public class MinerSpringSubTest {

    @Test
    public void testMiner() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "SUB_DATA",
                "name=Joe\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val testMiner = SpringContext.getBean(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals("Joe", testMiner.name());
        assertEquals("Joe Doe", testMiner.full());
        assertEquals("Joe Doe Richard", testMiner.longName());
        assertEquals("Joe Doe Richard", testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));
        assertEquals("springspring&springspring&guiceguice",
                testMiner.defaultInContext());

        val testMinerSub = SpringContext.getBean(TestMinerSub.class);
        assertNotNull(testMinerSub);
        assertEquals("Joe", testMinerSub.name());
        assertEquals("Joe Doe", testMinerSub.full());
        assertEquals("Joe Doe Richard", testMinerSub.longName());
        assertEquals("Joe Doe Richard", testMinerSub.longWrap());
        assertEquals("xyz", testMinerSub.abc("xyz"));
        assertNull(testMinerSub.abc(null));
        assertEquals("springspring&springspring&guiceguice",
                testMinerSub.defaultInContext());
    }
}
