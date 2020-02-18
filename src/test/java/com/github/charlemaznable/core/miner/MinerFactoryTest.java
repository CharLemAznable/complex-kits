package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.miner.MinerConfig.DataIdProvider;
import com.github.charlemaznable.core.miner.MinerConfig.DefaultValueProvider;
import com.github.charlemaznable.core.miner.MinerConfig.GroupProvider;
import com.google.common.base.Splitter;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.joor.ReflectException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.Minerable;
import org.n3r.diamond.client.cache.ParamsAppliable;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import static com.github.charlemaznable.core.miner.MinerFactory.getMiner;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinerFactoryTest {

    @BeforeAll
    public static void beforeClass() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("Env", "miner",
                "prop=PROP\ndefault=DEFAULT");
    }

    @AfterAll
    public static void afterClass() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testConstruct() {
        assertThrows(ReflectException.class,
                () -> onClass(MinerFactory.class).create().get());
        assertThrows(ReflectException.class,
                () -> onClass(MinerElf.class).create().get());
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
        assertEquals('a', minerDefault.charValue());

        assertEquals(0, minerDefault.shortValueDefault());
        assertEquals(0, minerDefault.intValueDefault());
        assertEquals(0, minerDefault.longValueDefault());
        assertEquals(0, minerDefault.floatValueDefault());
        assertEquals(0, minerDefault.doubleValueDefault());
        assertEquals(0, minerDefault.byteValueDefault());
        assertEquals('\0', minerDefault.charValueDefault());

        val minerDefaultData = getMiner(MinerDefaultData.class);
        val properties = minerDefaultData.properties();
        System.out.println(properties);

        assertEquals("John", properties.getProperty("name"));
        assertEquals("John Doe", properties.getProperty("full"));
        assertEquals("John Doe Richard", properties.getProperty("long"));

        assertEquals("yes", properties.getProperty("testMode"));
        assertEquals("TRUE", properties.getProperty("testMode2"));

        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe Richard)",
                properties.getProperty("content"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John) @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe) @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe Richard)",
                properties.getProperty("list"));
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

        assertThrows(MinerConfigException.class, () -> getMiner(ProvideError1.class));
        assertThrows(MinerConfigException.class, () -> getMiner(ProvideError2.class));
        val error3 = getMiner(ProvideError3.class);
        assertThrows(MinerConfigException.class, error3::prop);
        val error4 = getMiner(ProvideError4.class);
        assertThrows(MinerConfigException.class, error4::prop);
        val error5 = getMiner(ProvideError5.class);
        assertThrows(MinerConfigException.class, error5::prop);
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
        char charValue();

        short shortValueDefault();

        int intValueDefault();

        long longValueDefault();

        float floatValueDefault();

        double doubleValueDefault();

        byte byteValueDefault();

        char charValueDefault();
    }

    @MinerConfig
    public interface MinerDefaultData {

        @MinerConfig("DEFAULT_DATA")
        Properties properties();
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

    @MinerConfig(
            groupProvider = TestGroupProvider.class,
            dataIdProvider = TestDataIdProvider.class
    )
    interface StoneProps {

        String name();

        String full();

        @MinerConfig("long")
        String longName();

        @MinerConfig(
                groupProvider = TestGroupProvider.class,
                dataIdProvider = TestDataIdProvider.class,
                defaultValueProvider = TestDefaultValueProvider.class
        )
        String prop();
    }

    public static class TestGroupProvider implements GroupProvider {

        @Override
        public String group(Class<?> minerClass) {
            assertEquals(StoneProps.class, minerClass);
            return "${group}Group";
        }

        @Override
        public String group(Class<?> minerClass, Method method) {
            assertEquals(StoneProps.class, minerClass);
            assertEquals("prop", method.getName());
            return "";
        }
    }

    public static class TestDataIdProvider implements DataIdProvider {

        @Override
        public String dataId(Class<?> minerClass) {
            assertEquals(StoneProps.class, minerClass);
            return "Data${data}";
        }

        @Override
        public String dataId(Class<?> minerClass, Method method) {
            assertEquals(StoneProps.class, minerClass);
            assertEquals("prop", method.getName());
            return "Prop${prop}";
        }
    }

    public static class TestDefaultValueProvider implements DefaultValueProvider {

        @Override
        public String defaultValue(Class<?> minerClass, Method method) {
            assertEquals(StoneProps.class, minerClass);
            assertEquals("prop", method.getName());
            return "${default}Default";
        }
    }

    @MinerConfig(
            groupProvider = ErrorGroupProvider.class
    )
    interface ProvideError1 {}

    @MinerConfig(
            groupProvider = NoErrorGroupProvider.class,
            dataIdProvider = ErrorDataIdProvider.class
    )
    interface ProvideError2 {}

    @MinerConfig(
            groupProvider = NoErrorGroupProvider.class,
            dataIdProvider = NoErrorDataIdProvider.class
    )
    interface ProvideError3 {

        @MinerConfig(
                groupProvider = ErrorGroupProvider.class
        )
        String prop();
    }

    @MinerConfig(
            groupProvider = NoErrorGroupProvider.class,
            dataIdProvider = NoErrorDataIdProvider.class
    )
    interface ProvideError4 {

        @MinerConfig(
                groupProvider = NoErrorGroupProvider.class,
                dataIdProvider = ErrorDataIdProvider.class
        )
        String prop();
    }

    @MinerConfig(
            groupProvider = NoErrorGroupProvider.class,
            dataIdProvider = NoErrorDataIdProvider.class
    )
    interface ProvideError5 {

        @MinerConfig(
                groupProvider = NoErrorGroupProvider.class,
                dataIdProvider = NoErrorDataIdProvider.class,
                defaultValueProvider = ErrorDefaultValueProvider.class
        )
        String prop();
    }

    public static class ErrorGroupProvider implements GroupProvider {}

    public static class ErrorDataIdProvider implements DataIdProvider {}

    public static class ErrorDefaultValueProvider implements DefaultValueProvider {}

    public static class NoErrorGroupProvider implements GroupProvider {

        @Override
        public String group(Class<?> minerClass) {
            return "${group}Group";
        }

        @Override
        public String group(Class<?> minerClass, Method method) {
            return "";
        }
    }

    public static class NoErrorDataIdProvider implements DataIdProvider {

        @Override
        public String dataId(Class<?> minerClass) {
            return "Data${data}";
        }

        @Override
        public String dataId(Class<?> minerClass, Method method) {
            return "Prop${prop}";
        }
    }
}
