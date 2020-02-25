package com.github.charlemaznable.core.miner.guice;

import com.github.charlemaznable.core.miner.MinerConfigException;
import com.github.charlemaznable.core.miner.MinerInjector;
import com.github.charlemaznable.core.miner.testminer.TestMiner;
import com.github.charlemaznable.core.miner.testminer.TestMinerConcrete;
import com.github.charlemaznable.core.miner.testminer.TestMinerDataId;
import com.github.charlemaznable.core.miner.testminer.TestMinerNone;
import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.lang.reflect.Method;

import static java.util.Collections.emptyList;
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
                bind(TestMinerDataId.class).toProvider(Providers.of(new TestMinerDataId() {
                    @Override
                    public String dataId(Class<?> minerClass, Method method) {
                        return "long";
                    }
                }));
            }
        });
        val injector = minerInjector.createInjector(
                TestMiner.class, TestMinerConcrete.class, TestMinerNone.class);

        val testMiner = injector.getInstance(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals("John", testMiner.name());
        assertEquals("John Doe", testMiner.full());
        assertEquals("John Doe Richard", testMiner.longName());
        assertEquals("John Doe Richard", testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));
        assertEquals("guiceguice&springspring&guiceguice",
                testMiner.defaultInContext());
        assertEquals("John", testMiner.name());
        assertEquals("John Doe", testMiner.full());
        assertEquals("John Doe Richard", testMiner.longName());
        assertEquals("John Doe Richard", testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));
        assertEquals("guiceguice&springspring&guiceguice",
                testMiner.defaultInContext());

        val testMinerConcrete = injector.getInstance(TestMinerConcrete.class);
        assertNull(testMinerConcrete);

        val testMinerNone = injector.getInstance(TestMinerNone.class);
        assertNull(testMinerNone);
    }

    @Test
    public void testMinerError() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");
        val minerInjector = new MinerInjector(emptyList());
        val injector = minerInjector.createInjector(
                TestMiner.class, TestMinerConcrete.class, TestMinerNone.class);

        val testMiner = injector.getInstance(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals("John", testMiner.name());
        assertEquals("John Doe", testMiner.full());
        assertThrows(NullPointerException.class, testMiner::longName);
        assertNull(testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));

        val testMinerConcrete = injector.getInstance(TestMinerConcrete.class);
        assertNull(testMinerConcrete);

        val testMinerNone = injector.getInstance(TestMinerNone.class);
        assertNull(testMinerNone);
    }

    @Test
    public void testMinerNaked() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");
        val minerInjector = new MinerInjector();

        val testMiner = minerInjector.getMiner(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals("John", testMiner.name());
        assertEquals("John Doe", testMiner.full());
        assertThrows(NullPointerException.class, testMiner::longName);
        assertNull(testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));

        assertThrows(MinerConfigException.class,
                () -> minerInjector.getMiner(TestMinerConcrete.class));

        assertThrows(MinerConfigException.class,
                () -> minerInjector.getMiner(TestMinerNone.class));
    }
}
