package com.github.charlemaznable.core.miner.spring;

import com.github.charlemaznable.core.miner.testminer.TestMiner;
import com.github.charlemaznable.core.miner.testminer.TestMinerConcrete;
import com.github.charlemaznable.core.miner.testminer.TestMinerNone;
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
@ContextConfiguration(classes = MinerSpringConfiguration.class)
public class MinerSpringTest {

    @Test
    public void testMiner() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val testMiner = SpringContext.getBean(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals("John", testMiner.name());
        assertEquals("John Doe", testMiner.full());
        assertEquals("John Doe Richard", testMiner.longName());
        assertEquals("John Doe Richard", testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));
        assertEquals("springspring&springspring&guiceguice",
                testMiner.defaultInContext());
        assertEquals("John", testMiner.name());
        assertEquals("John Doe", testMiner.full());
        assertEquals("John Doe Richard", testMiner.longName());
        assertEquals("John Doe Richard", testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));
        assertEquals("springspring&springspring&guiceguice",
                testMiner.defaultInContext());

        val testMinerConcrete = SpringContext.getBean(TestMinerConcrete.class);
        assertNull(testMinerConcrete);

        val testMinerNone = SpringContext.getBean(TestMinerNone.class);
        assertNull(testMinerNone);
    }
}
