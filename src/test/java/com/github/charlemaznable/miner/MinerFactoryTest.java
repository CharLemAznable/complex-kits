package com.github.charlemaznable.miner;

import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.Minerable;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static com.github.charlemaznable.miner.MinerFactory.getMiner;
import static com.github.charlemaznable.miner.MinerFactory.getStone;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    interface StoneDefault {}

    @MinerConfig("stone.data")
    interface StoneDefaultGroup {}

    @MinerConfig(group = "stone.group", dataId = "stone.data")
    interface StoneDefaultNone {}

    @Test
    public void testStone() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA", "defaultData");
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "stone.data", "defaultGroup");
        MockDiamondServer.setConfigInfo("stone.group", "stone.data", "abc");

        val stoneDefault = getStone(StoneDefault.class);
        assertEquals("defaultData", stoneDefault);

        val stoneDefaultGroup = getStone(StoneDefaultGroup.class);
        assertEquals("defaultGroup", stoneDefaultGroup);

        val stoneDefaultNone = getStone(StoneDefaultNone.class);
        assertEquals("abc", stoneDefaultNone);
    }

    @MinerConfig
    public interface MinerDefault {

        String name();

        String full();

        @MinerProperty("long")
        String longName();

        @MinerProperty(defaultValue = "abc")
        String abc(String defaultValue);

        String xyz(String defaultValue);
    }

    @Test
    public void testMiner() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val minerDefault = getMiner(MinerDefault.class);
        assertNotNull(minerDefault);
        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertEquals("John Doe Richard", minerDefault.longName());
        assertEquals("abc", minerDefault.abc(null));
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertEquals("abc", minerDefault.xyz("abc"));
        assertThrows(NullPointerException.class, () -> minerDefault.xyz(null));
    }

    @MinerConfig
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
