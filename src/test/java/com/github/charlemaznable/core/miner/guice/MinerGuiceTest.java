package com.github.charlemaznable.core.miner.guice;

import com.github.charlemaznable.core.guice.GuiceFactory;
import com.github.charlemaznable.core.miner.MinerConfigException;
import com.github.charlemaznable.core.miner.MinerModular;
import com.github.charlemaznable.core.miner.testminer.TestMiner;
import com.github.charlemaznable.core.miner.testminer.TestMinerConcrete;
import com.github.charlemaznable.core.miner.testminer.TestMinerDataId;
import com.github.charlemaznable.core.miner.testminer.TestMinerNone;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
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

    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    private static final String DEFAULT_DATA = "DEFAULT_DATA";
    private static final String SUB_DATA = "SUB_DATA";
    private static final String DEFAULT_CONTENT = "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard";
    private static final String SUB_CONTENT = "name=Joe\nfull=${this.name} Doe\nlong=${this.full} Richard";
    private static final String NAME = "John";
    private static final String FULL = "John Doe";
    private static final String LONG = "John Doe Richard";
    private static final String XYZ = "xyz";
    private static final String SUB_NAME = "Joe";
    private static final String SUB_FULL = "Joe Doe";
    private static final String SUB_LONG = "Joe Doe Richard";
    private static final String GUICE_CONTEXT = "guiceguice&springspring&guiceguice";

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
        MockDiamondServer.setConfigInfo(DEFAULT_GROUP, DEFAULT_DATA, DEFAULT_CONTENT);
        val minerInjector = new MinerModular(new AbstractModule() {
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
        val injector = Guice.createInjector(minerInjector.createModule(
                TestMiner.class, TestMinerConcrete.class, TestMinerNone.class));

        val testMiner = injector.getInstance(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals(NAME, testMiner.name());
        assertEquals(FULL, testMiner.full());
        assertEquals(LONG, testMiner.longName());
        assertEquals(LONG, testMiner.longWrap());
        assertEquals(XYZ, testMiner.abc(XYZ));
        assertNull(testMiner.abc(null));
        assertEquals(GUICE_CONTEXT, testMiner.defaultInContext());
        assertEquals(NAME, testMiner.name());
        assertEquals(FULL, testMiner.full());
        assertEquals(LONG, testMiner.longName());
        assertEquals(LONG, testMiner.longWrap());
        assertEquals(XYZ, testMiner.abc(XYZ));
        assertNull(testMiner.abc(null));
        assertEquals(GUICE_CONTEXT, testMiner.defaultInContext());

        val testMinerConcrete = injector.getInstance(TestMinerConcrete.class);
        assertNull(testMinerConcrete);

        val testMinerNone = injector.getInstance(TestMinerNone.class);
        assertNull(testMinerNone);
    }

    @Test
    public void testMinerError() {
        MockDiamondServer.setConfigInfo(DEFAULT_GROUP, DEFAULT_DATA, DEFAULT_CONTENT);
        val minerInjector = new MinerModular(emptyList());
        val injector = Guice.createInjector(minerInjector.createModule(
                TestMiner.class, TestMinerConcrete.class, TestMinerNone.class));

        val testMiner = injector.getInstance(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals(NAME, testMiner.name());
        assertEquals(FULL, testMiner.full());
        assertThrows(NullPointerException.class, testMiner::longName);
        assertNull(testMiner.longWrap());
        assertEquals(XYZ, testMiner.abc(XYZ));
        assertNull(testMiner.abc(null));

        val testMinerConcrete = injector.getInstance(TestMinerConcrete.class);
        assertNull(testMinerConcrete);

        val testMinerNone = injector.getInstance(TestMinerNone.class);
        assertNull(testMinerNone);
    }

    @Test
    public void testMinerNaked() {
        MockDiamondServer.setConfigInfo(DEFAULT_GROUP, DEFAULT_DATA, DEFAULT_CONTENT);
        val minerInjector = new MinerModular();

        val testMiner = minerInjector.getMiner(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals(NAME, testMiner.name());
        assertEquals(FULL, testMiner.full());
        assertThrows(NullPointerException.class, testMiner::longName);
        assertNull(testMiner.longWrap());
        assertEquals(XYZ, testMiner.abc(XYZ));
        assertNull(testMiner.abc(null));

        assertThrows(MinerConfigException.class,
                () -> minerInjector.getMiner(TestMinerConcrete.class));

        assertThrows(MinerConfigException.class,
                () -> minerInjector.getMiner(TestMinerNone.class));

        val injector = Guice.createInjector(minerInjector.createModule());
        assertThrows(ConfigurationException.class, () ->
                injector.getInstance(TestMiner.class));
        assertNull(new GuiceFactory(injector).build(TestMiner.class));
    }

    @Test
    public void testMinerSub() {
        MockDiamondServer.setConfigInfo(DEFAULT_GROUP, DEFAULT_DATA, DEFAULT_CONTENT);
        MockDiamondServer.setConfigInfo(DEFAULT_GROUP, SUB_DATA, SUB_CONTENT);
        val minerInjector = new MinerModular(new AbstractModule() {
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
        val injector = Guice.createInjector(minerInjector.createModule(TestMinerSub.class));

        val testMiner = injector.getInstance(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals(SUB_NAME, testMiner.name());
        assertEquals(SUB_FULL, testMiner.full());
        assertEquals(SUB_LONG, testMiner.longName());
        assertEquals(SUB_LONG, testMiner.longWrap());
        assertEquals(XYZ, testMiner.abc(XYZ));
        assertNull(testMiner.abc(null));
        assertEquals(GUICE_CONTEXT, testMiner.defaultInContext());

        val testMinerSub = injector.getInstance(TestMinerSub.class);
        assertNotNull(testMinerSub);
        assertEquals(SUB_NAME, testMinerSub.name());
        assertEquals(SUB_FULL, testMinerSub.full());
        assertEquals(SUB_LONG, testMinerSub.longName());
        assertEquals(SUB_LONG, testMinerSub.longWrap());
        assertEquals(XYZ, testMinerSub.abc(XYZ));
        assertNull(testMinerSub.abc(null));
        assertEquals(GUICE_CONTEXT, testMinerSub.defaultInContext());
    }
}
