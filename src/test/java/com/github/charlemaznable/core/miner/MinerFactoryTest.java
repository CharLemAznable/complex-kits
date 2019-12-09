package com.github.charlemaznable.core.miner;

import com.google.common.base.Splitter;
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

import static com.github.charlemaznable.core.miner.MinerFactory.getMiner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    public void testStone() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "stone.data", "abc");
        MockDiamondServer.setConfigInfo("stone.group", "stone.data", "xyz");

        val stoneDefault = getMiner(StoneDefault.class);
        assertEquals("abc", stoneDefault.abc());
        assertEquals("xyz", stoneDefault.xyz());

        assertThrows(MinerConfigException.class, () -> getMiner(StoneError.class));
    }

    @SneakyThrows
    @Test
    public void testMiner() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard\n" +
                        "testMode=yes\ntestMode2=TRUE\n" +
                        "content=@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.long})\n" +
                        "list=@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.name}) " +
                        "@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.full}) " +
                        "@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.long})");

        val minerDefault = getMiner(MinerDefault.class);
        assertNotNull(minerDefault);

        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertEquals("John Doe Richard", minerDefault.longName());

        assertEquals(3, minerDefault.longSplit().size());
        assertEquals("John", minerDefault.longSplit().get(0));
        assertEquals("Doe", minerDefault.longSplit().get(1));
        assertEquals("Richard", minerDefault.longSplit().get(2));

        assertEquals("xyz", minerDefault.abc("xyz"));
        assertEquals("abc", minerDefault.abc(null));

        assertEquals(3, minerDefault.count(3));
        assertEquals(0, minerDefault.count(null));
        assertEquals(1, minerDefault.count1());

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

        assertEquals(10, minerDefault.shortValue());
        assertEquals(200, minerDefault.intValue());
        assertEquals(3000L, minerDefault.longValue());
        assertEquals(40000F, minerDefault.floatValue());
        assertEquals(5D, minerDefault.doubleValue());
        assertEquals('a', minerDefault.byteValue());
        assertNull(minerDefault.charValue());
    }

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

    @Test
    public void testStoneProps() {
        MockDiamondServer.setConfigInfo("GROUPGroup", "DataDATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val stoneProps = getMiner(StoneProps.class);
        assertNotNull(stoneProps);
        assertEquals("John", stoneProps.name());
        assertEquals("John Doe", stoneProps.full());
        assertEquals("John Doe Richard", stoneProps.longName());
        assertEquals("DEFAULTDefault", stoneProps.prop());
    }

    @MinerConfig
    interface StoneDefault {

        @MinerConfig("stone.data")
        String abc();

        @MinerConfig(group = "stone.group", dataId = "stone.data")
        String xyz();
    }

    @MinerConfig("DEFAULT_DATA")
    public interface MinerDefault {

        String name();

        String full();

        @MinerConfig("long")
        String longName();

        default List<String> longSplit() {
            return Splitter.on(" ").omitEmptyStrings()
                    .trimResults().splitToList(longName());
        }

        @MinerConfig(defaultValue = "abc")
        String abc(String defaultValue);

        int count(Integer defaultValue);

        @MinerConfig(defaultValue = "1")
        int count1();

        boolean testMode();

        Boolean testMode2();

        MinerContentBean content();

        List<MinerContentBean> list();

        @MinerConfig(defaultValue = "10")
        short shortValue();

        @MinerConfig(defaultValue = "200")
        int intValue();

        @MinerConfig(defaultValue = "3000")
        long longValue();

        @MinerConfig(defaultValue = "40000")
        float floatValue();

        @MinerConfig(defaultValue = "5")
        double doubleValue();

        @MinerConfig(defaultValue = "97")
        byte byteValue();

        @MinerConfig(defaultValue = "a")
        Character charValue();
    }

    @MinerConfig("DEFAULT_DATA")
    public interface MinerableDefault extends Minerable {}

    @Data
    public static class MinerContentBean implements ParamsAppliable {

        private String name;

        @Override
        public void applyParams(String[] strings) {
            if (strings.length > 0) this.name = strings[0];
        }
    }

    @MinerConfig
    class StoneError {}

    @MinerConfig(group = "${group}Group", dataId = "Data${data}")
    interface StoneProps {

        String name();

        String full();

        @MinerConfig("long")
        String longName();

        @MinerConfig(dataId = "Prop${prop}", defaultValue = "${default}Default")
        String prop();
    }
}
