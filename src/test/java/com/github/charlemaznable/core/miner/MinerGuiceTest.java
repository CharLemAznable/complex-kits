package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.miner.testClass.TestMiner;
import com.github.charlemaznable.core.miner.testClass.TestMiner2;
import com.github.charlemaznable.core.miner.testClass.TestMinerDataId;
import com.github.charlemaznable.core.miner.testClass.TestMinerDataIdImpl;
import com.github.charlemaznable.core.miner.testClass.TestMinerNone;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import lombok.val;
import lombok.var;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.google.inject.Scopes.SINGLETON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MinerGuiceTest {

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testMiner() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "SUB_DATA",
                "name=Joe\nfull=${this.name} Doe\nlong=${this.full} Richard");
        val minerInjector = new MinerInjector(new AbstractModule() {
            @Override
            public void configure() {
                bind(TestMinerDataId.class).to(TestMinerDataIdImpl.class).in(SINGLETON);
            }
        });
        val injector = minerInjector.createInjector(
                TestMiner.class, TestMiner2.class, TestMinerNone.class);

        var minerDefault = injector.getInstance(TestMiner.class);
        assertNotNull(minerDefault);
        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertEquals("John Doe Richard", minerDefault.longName());
        assertEquals("John Doe Richard", minerDefault.longWrap());
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertNull(minerDefault.abc(null));

        assertNull(injector.getInstance(TestMiner2.class));

        assertThrows(ConfigurationException.class, () ->
                injector.getInstance(TestMinerNone.class));

        val minerInjector2 = new MinerInjector(newArrayList(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TestMiner2.class).in(SINGLETON);
            }
        }));
        val injector2 = Guice.createInjector(minerInjector2.createModule(
                TestMinerSub.class, TestMiner2.class, TestMinerNone.class));
        minerDefault = injector2.getInstance(TestMiner.class);
        assertNotNull(minerDefault);
        assertEquals("Joe", minerDefault.name());
        assertEquals("Joe Doe", minerDefault.full());
        val finalMinerDefault1 = minerDefault;
        assertThrows(ConfigurationException.class, finalMinerDefault1::longName);
        assertNull(minerDefault.longWrap());
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertNull(minerDefault.abc(null));

        assertNotNull(injector2.getInstance(TestMiner2.class));

        assertThrows(ConfigurationException.class, () ->
                injector2.getInstance(TestMinerNone.class));
    }

    @MinerConfig("SUB_DATA")
    public interface TestMinerSub extends TestMiner {
    }
}
