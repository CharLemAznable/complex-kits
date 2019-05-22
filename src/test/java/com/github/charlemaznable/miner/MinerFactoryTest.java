package com.github.charlemaznable.miner;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.Minerable;
import org.n3r.diamond.client.cache.ParamsAppliable;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.util.List;

import static com.github.charlemaznable.miner.MinerFactory.getMiner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinerFactoryTest {

    @BeforeAll
    public static void beforeClass() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterClass() {
        MockDiamondServer.tearDownMockServer();
    }

    @MinerConfig
    interface StoneDefault {

        @MinerConfig("stone.data")
        String abc();

        @MinerConfig(group = "stone.group", dataId = "stone.data")
        String xyz();
    }

    @Test
    public void testStone() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "stone.data", "abc");
        MockDiamondServer.setConfigInfo("stone.group", "stone.data", "xyz");

        val stoneDefault = getMiner(StoneDefault.class);
        assertEquals("abc", stoneDefault.abc());
        assertEquals("xyz", stoneDefault.xyz());
    }

    @Data
    public static class MinerContentBean implements ParamsAppliable {

        private String name;

        @Override
        public void applyParams(String[] strings) {
            if (strings.length > 0) this.name = strings[0];
        }
    }

    @MinerConfig("DEFAULT_DATA")
    public interface MinerDefault {

        String name();

        String full();

        @MinerConfig("long")
        String longName();

        String abc(String defaultValue);

        boolean testMode();

        Boolean testMode2();

        MinerContentBean content();

        List<MinerContentBean> list();
    }

    @SneakyThrows
    @Test
    public void testMiner() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard\n" +
                        "testMode=yes\ntestMode2=TRUE\n" +
                        "content=@com.github.charlemaznable.miner.MinerFactoryTest$MinerContentBean(${this.long})\n" +
                        "list=@com.github.charlemaznable.miner.MinerFactoryTest$MinerContentBean(${this.name}) " +
                        "@com.github.charlemaznable.miner.MinerFactoryTest$MinerContentBean(${this.full}) " +
                        "@com.github.charlemaznable.miner.MinerFactoryTest$MinerContentBean(${this.long})");

        val minerDefault = getMiner(MinerDefault.class);
        assertNotNull(minerDefault);
        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertEquals("John Doe Richard", minerDefault.longName());
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertNull(minerDefault.abc(null));
        assertTrue(minerDefault.testMode());
        assertEquals(Boolean.TRUE, minerDefault.testMode2());
        assertEquals("John Doe Richard", minerDefault.content().getName());
        assertEquals(3, minerDefault.list().size());
        assertEquals("John", minerDefault.list().get(0).getName());
        assertEquals("John Doe", minerDefault.list().get(1).getName());
        assertEquals("John Doe Richard", minerDefault.list().get(2).getName());

        val minerableDefault = (Minerable) minerDefault;
        assertNotNull(minerableDefault);
        assertEquals("John", minerableDefault.getString("name"));
        assertEquals("John Doe", minerableDefault.getString("full"));
        assertEquals("John Doe Richard", minerableDefault.getString("long"));
    }

    @MinerConfig("DEFAULT_DATA")
    public interface MinerableDefault extends Minerable {}

    @Test
    public void testMinerable() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val minerableDefault = getMiner(MinerableDefault.class);
        assertNotNull(minerableDefault);
        assertEquals("John", minerableDefault.getString("name"));
        assertEquals("John Doe", minerableDefault.getString("full"));
        assertEquals("John Doe Richard", minerableDefault.getString("long"));
    }
}
