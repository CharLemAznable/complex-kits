package com.github.charlemaznable.core.miner.spring;

import com.github.charlemaznable.core.miner.MinerConfigException;
import com.github.charlemaznable.core.miner.testminer.TestMiner;
import com.github.charlemaznable.core.miner.testminer.TestMinerConcrete;
import com.github.charlemaznable.core.miner.testminer.TestMinerNone;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static com.github.charlemaznable.core.miner.MinerFactory.getMiner;
import static com.github.charlemaznable.core.miner.MinerFactory.springMinerLoader;
import static org.joor.Reflect.on;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MinerSpringNakedTest {

    @BeforeAll
    public static void beforeAll() {
        on(springMinerLoader()).field("minerCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testMinerNaked() {
        val SpringContextClass = onClass(SpringContext.class);
        val applicationContext = SpringContextClass.field("applicationContext").get();
        SpringContextClass.set("applicationContext", null);
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val testMiner = getMiner(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals("John", testMiner.name());
        assertEquals("John Doe", testMiner.full());
        assertThrows(NullPointerException.class, testMiner::longName);
        assertNull(testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));

        assertThrows(MinerConfigException.class,
                () -> getMiner(TestMinerConcrete.class));

        assertThrows(MinerConfigException.class,
                () -> getMiner(TestMinerNone.class));

        SpringContextClass.set("applicationContext", applicationContext);
    }
}
