package com.github.charlemaznable.core.miner.spring;

import com.github.charlemaznable.core.miner.testminer.TestMiner;
import com.github.charlemaznable.core.miner.testminer.TestMinerConcrete;
import com.github.charlemaznable.core.miner.testminer.TestMinerNone;
import com.github.charlemaznable.core.spring.SpringContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MinerSpringErrorConfiguration.class)
public class MinerSpringErrorTest {

    @Test
    public void testMinerError() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        var testMiner = SpringContext.getBean(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals("John", testMiner.name());
        assertEquals("John Doe", testMiner.full());
        assertThrows(NullPointerException.class, testMiner::longName);
        assertNull(testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));

        var testMinerConcrete = SpringContext.getBean(TestMinerConcrete.class);
        assertNull(testMinerConcrete);

        var testMinerNone = SpringContext.getBean(TestMinerNone.class);
        assertNull(testMinerNone);
    }
}
