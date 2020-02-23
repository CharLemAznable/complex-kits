package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.miner.testClass.TestMiner;
import com.github.charlemaznable.core.miner.testClass.TestMiner2;
import com.github.charlemaznable.core.miner.testClass.TestMinerDataId;
import com.github.charlemaznable.core.miner.testClass.TestMinerDataIdImpl;
import com.github.charlemaznable.core.miner.testClass.TestMinerDataIdProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import lombok.val;
import lombok.var;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

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
        val minerInjector = new MinerInjector(new AbstractModule() {
            @Override
            public void configure() {
                bind(TestMinerDataId.class).to(TestMinerDataIdImpl.class).in(SINGLETON);
                bind(TestMinerDataIdProvider.class).in(SINGLETON);
            }
        });
        var injector = minerInjector.injectMiner(TestMiner.class);

        var minerDefault = injector.getInstance(TestMiner.class);
        assertNotNull(minerDefault);
        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertEquals("John Doe Richard", minerDefault.longName());
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertNull(minerDefault.abc(null));

        minerDefault = minerInjector.getMiner(TestMiner.class);
        assertNotNull(minerDefault);
        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertEquals("John Doe Richard", minerDefault.longName());
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertNull(minerDefault.abc(null));

        assertThrows(MinerConfigException.class, () ->
                minerInjector.getMiner(TestMiner2.class));

        val minerInjector2 = new MinerInjector();
        injector = Guice.createInjector(minerInjector2.minerModule(TestMiner.class));
        minerDefault = injector.getInstance(TestMiner.class);
        assertNotNull(minerDefault);
        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertNull(minerDefault.longName());
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertNull(minerDefault.abc(null));

        minerDefault = minerInjector2.getMiner(TestMiner.class);
        assertNotNull(minerDefault);
        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertNull(minerDefault.longName());
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertNull(minerDefault.abc(null));

        assertThrows(MinerConfigException.class, () ->
                minerInjector2.getMiner(TestMiner2.class));
    }
}