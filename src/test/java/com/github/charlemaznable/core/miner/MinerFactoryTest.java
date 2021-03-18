package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.config.Arguments;
import com.github.charlemaznable.core.miner.MinerConfig.DataIdProvider;
import com.github.charlemaznable.core.miner.MinerConfig.DefaultValueProvider;
import com.github.charlemaznable.core.miner.MinerConfig.GroupProvider;
import com.github.charlemaznable.core.miner.MinerFactory.MinerLoader;
import com.github.charlemaznable.core.miner.MinerStoneParse.MinerStoneParser;
import com.github.charlemaznable.core.miner.parser.VertxOptionsParser;
import com.github.charlemaznable.vertx.diamond.VertxDiamondElf;
import com.google.common.base.Splitter;
import io.vertx.core.VertxOptions;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.text.StringSubstitutor;
import org.joor.ReflectException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.Minerable;
import org.n3r.diamond.client.cache.ParamsAppliable;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Await.awaitOfMicros;
import static com.github.charlemaznable.core.lang.Await.awaitOfMillis;
import static com.github.charlemaznable.core.lang.Await.awaitOfSeconds;
import static com.github.charlemaznable.core.miner.MinerElf.minerAsSubstitutor;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinerFactoryTest {

    private static MinerLoader minerLoader = MinerFactory.minerLoader(reflectFactory());

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("Env", "miner",
                "prop=PROP\ndefault=DEFAULT");
    }

    @AfterAll
    public static void afterAll() {
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

        val stoneDefault = minerLoader.getMiner(StoneDefault.class);
        assertEquals("abc", stoneDefault.abc());
        assertEquals("xyz", stoneDefault.xyz());

        assertEquals("Miner@" + Integer.toHexString(stoneDefault.hashCode()), stoneDefault.toString());
        assertEquals(stoneDefault, stoneDefault);

        assertThrows(MinerConfigException.class,
                () -> minerLoader.getMiner(StoneConcrete.class));

        assertThrows(MinerConfigException.class,
                () -> minerLoader.getMiner(StoneNone.class));
    }

    @Test
    public void testCache() {
        MockDiamondServer.setConfigInfo("CACHE_GROUP", "CACHE_KEY", "key1=value1");

        val testNoCache = minerLoader.getMiner(TestNoCache.class);
        val testCache = minerLoader.getMiner(TestCache.class);
        assertEquals("value1", testNoCache.key1());
        assertEquals("value1", testCache.key1());
        assertNull(testNoCache.key2());
        assertNull(testCache.key2());

        MockDiamondServer.setConfigInfo("CACHE_GROUP", "CACHE_KEY", "key2=value2");
        awaitOfMillis(100);
        assertNull(testNoCache.key1());
        assertEquals("value1", testCache.key1());
        assertEquals("value2", testNoCache.key2());
        assertNull(testCache.key2());

        awaitOfSeconds(2);
        assertNull(testNoCache.key1());
        assertNull(testCache.key1());
        assertEquals("value2", testNoCache.key2());
        assertEquals("value2", testCache.key2());
    }

    @SneakyThrows
    @Test
    public void testMiner() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA", "" +
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard\n" +
                "testMode=yes\ntestMode2=TRUE\n" +
                "content=@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.long})\n" +
                "list=@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.name}) " +
                "@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.full}) " +
                "@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.long})");

        val minerDefault = minerLoader.getMiner(MinerDefault.class);
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

        val minerDefaultData = minerLoader.getMiner(MinerDefaultData.class);
        val properties = minerDefaultData.properties();

        assertEquals("John", properties.getProperty("name"));
        assertEquals("John Doe", properties.getProperty("full"));
        assertEquals("John Doe Richard", properties.getProperty("long"));

        assertEquals("yes", properties.getProperty("testMode"));
        assertEquals("TRUE", properties.getProperty("testMode2"));

        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe Richard)",
                properties.getProperty("content"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John) @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe) @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe Richard)",
                properties.getProperty("list"));

        assertNotEquals(minerDefault.hashCode(), minerDefaultData.hashCode());
        assertNotEquals(minerDefault, minerDefaultData);
    }

    @Test
    public void testMinerable() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val minerableDefault = minerLoader.getMiner(MinerableDefault.class);
        assertNotNull(minerableDefault);
        assertEquals("John", minerableDefault.getString("name"));
        assertEquals("John Doe", minerableDefault.getString("full"));
        assertEquals("John Doe Richard", minerableDefault.getString("long"));

        awaitOfMillis(100);

        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "# toml\nname=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val minerableDefaultError = minerLoader.getMiner(MinerableDefault.class);
        assertNull(minerableDefaultError.getString("name"));
        assertNull(minerableDefaultError.getString("full"));
        assertNull(minerableDefaultError.getString("long"));

        awaitOfMillis(100);

        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "# TOML\nname='John'\nfull='John Doe'\nlong='John Doe Richard'");

        val minerableDefaultToml = minerLoader.getMiner(MinerableDefault.class);
        assertEquals("John", minerableDefaultToml.getString("name"));
        assertEquals("John Doe", minerableDefaultToml.getString("full"));
        assertEquals("John Doe Richard", minerableDefaultToml.getString("long"));
    }

    @Test
    public void testMinerDefault() {
        MockDiamondServer.setConfigInfo("DEF_GROUP", "DEF_DATA", "" +
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard\n" +
                "testMode=yes\ntestMode2=TRUE\n" +
                "content=@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.long})\n" +
                "list=@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.name}) " +
                "@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.full}) " +
                "@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(${this.long})");

        val minerDefaultData = minerLoader.getMiner(MinerDefData.class);

        val properties = minerDefaultData.properties();
        assertEquals("John", properties.getProperty("name"));
        assertEquals("John Doe", properties.getProperty("full"));
        assertEquals("John Doe Richard", properties.getProperty("long"));
        assertEquals("yes", properties.getProperty("testMode"));
        assertEquals("TRUE", properties.getProperty("testMode2"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe Richard)",
                properties.getProperty("content"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John) @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe) @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe Richard)",
                properties.getProperty("list"));

        val map = minerDefaultData.map();
        assertEquals("John", map.get("name"));
        assertEquals("John Doe", map.get("full"));
        assertEquals("John Doe Richard", map.get("long"));
        assertEquals("yes", map.get("testMode"));
        assertEquals("TRUE", map.get("testMode2"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe Richard)",
                map.get("content"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John) @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe) @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(John Doe Richard)",
                map.get("list"));

        awaitOfMicros(TimeUnit.MILLISECONDS.toMicros(100));

        MockDiamondServer.setConfigInfo("DEF_GROUP", "DEF_DATA", "# Toml\n" +
                "name='John'\nfull='John Doe'\nlong='John Doe Richard'\n" +
                "testMode='yes'\ntestMode2='TRUE'\n" +
                "content='@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John Doe Richard\")'\n" +
                "list='@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John\") " +
                "@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John Doe\") " +
                "@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John Doe Richard\")'");

        val minerDefaultDataToml = minerLoader.getMiner(MinerDefData.class);

        val propertiesToml = minerDefaultDataToml.properties();
        assertEquals("John", propertiesToml.getProperty("name"));
        assertEquals("John Doe", propertiesToml.getProperty("full"));
        assertEquals("John Doe Richard", propertiesToml.getProperty("long"));
        assertEquals("yes", propertiesToml.getProperty("testMode"));
        assertEquals("TRUE", propertiesToml.getProperty("testMode2"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John Doe Richard\")",
                propertiesToml.getProperty("content"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John\") @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John Doe\") @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John Doe Richard\")",
                propertiesToml.getProperty("list"));

        val mapToml = minerDefaultDataToml.map();
        assertEquals("John", mapToml.get("name"));
        assertEquals("John Doe", mapToml.get("full"));
        assertEquals("John Doe Richard", mapToml.get("long"));
        assertEquals("yes", mapToml.get("testMode"));
        assertEquals("TRUE", mapToml.get("testMode2"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John Doe Richard\")",
                mapToml.get("content"));
        assertEquals("@com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John\") @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John Doe\") @com.github.charlemaznable.core.miner.MinerFactoryTest$MinerContentBean(\"John Doe Richard\")",
                mapToml.get("list"));
    }

    @Test
    public void testMinerParse() {
        MockDiamondServer.setConfigInfo("PARSE_GROUP", "PARSE_DATA", "workerPoolSize=30\n");

        val minerParseData = minerLoader.getMiner(MinerParseData.class);

        val properties = minerParseData.properties();
        assertEquals("30", properties.getProperty("workerPoolSize"));

        val vertxOptions = minerParseData.vertxOptions();
        assertEquals(30, vertxOptions.getWorkerPoolSize());

        val parseRaw = minerParseData.parseRaw();
        assertEquals(30, parseRaw.getWorkerPoolSize());
    }

    @Test
    public void testStoneProps() {
        onClass(MinerFactory.class).call("substitute", "");
        StringSubstitutor minerMinerSubstitutor =
                onClass(MinerFactory.class).field("minerMinerSubstitutor").get();
        minerMinerSubstitutor.setVariableResolver(
                minerAsSubstitutor("Env", "miner").getStringLookup());
        MockDiamondServer.setConfigInfo("GROUPGroup", "DataDATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val stoneProps = minerLoader.getMiner(StoneProps.class);
        assertNotNull(stoneProps);
        assertEquals("John", stoneProps.name());
        assertEquals("John Doe", stoneProps.full());
        assertEquals("John Doe Richard", stoneProps.longName());
        assertEquals("DEFAULTDefault", stoneProps.prop());

        val error1 = minerLoader.getMiner(ProvideError1.class);
        assertThrows(MinerConfigException.class, error1::prop);
        val error2 = minerLoader.getMiner(ProvideError2.class);
        assertThrows(MinerConfigException.class, error2::prop);
        val error3 = minerLoader.getMiner(ProvideError3.class);
        assertThrows(MinerConfigException.class, error3::prop);
        val error4 = minerLoader.getMiner(ProvideError4.class);
        assertThrows(MinerConfigException.class, error4::prop);
        val error5 = minerLoader.getMiner(ProvideError5.class);
        assertThrows(MinerConfigException.class, error5::prop);
    }

    @Test
    public void testArgMiner() {
        MockDiamondServer.setConfigInfo("Arg", "data",
                "custom1.key1=value1\ncustom1.key2=value2\ncustom2.key1=value2\ncustom2.key2=value1\n");

        val argMiner = minerLoader.getMiner(ArgMiner.class);
        assertNull(argMiner.custom1());
        assertNull(argMiner.custom2());

        Arguments.initial("--customKey1=key1", "--customKey2=key2");
        assertEquals("value1", argMiner.custom1());
        assertEquals("value1", argMiner.custom2());

        Arguments.initial("--customKey1=key2", "--customKey2=key1");
        assertEquals("value2", argMiner.custom1());
        assertEquals("value2", argMiner.custom2());
    }

    @MinerConfig
    interface StoneDefault {

        @MinerConfig("stone.data")
        String abc();

        @MinerConfig(group = "stone.group", dataId = "stone.data")
        String xyz();
    }

    @MinerConfig(group = "CACHE_GROUP", dataId = "CACHE_KEY")
    public interface TestNoCache {

        String key1();

        String key2();
    }

    @MinerConfig(group = "CACHE_GROUP", dataId = "CACHE_KEY", cacheSeconds = 1)
    public interface TestCache {

        String key1();

        String key2();
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

    @MinerConfig(group = "DEF_GROUP")
    public interface MinerDefData {

        @MinerConfig("DEF_DATA")
        Properties properties();

        @MinerConfig("DEF_DATA")
        Map<String, Object> map();
    }

    @MinerConfig(group = "PARSE_GROUP")
    public interface MinerParseData {

        @MinerConfig("PARSE_DATA")
        @MinerStoneParse(MinerStoneParser.class)
        Properties properties();

        @MinerConfig("PARSE_DATA")
        @MinerStoneParse(VertxOptionsParser.class)
        VertxOptions vertxOptions();

        @MinerConfig("PARSE_DATA")
        String raw();

        default VertxOptions parseRaw() {
            return VertxDiamondElf.parseStoneToVertxOptions(raw());
        }
    }

    interface StoneNone {}

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

    @MinerConfig(
            groupProvider = ErrorGroupProvider.class
    )
    interface ProvideError1 {

        String prop();
    }

    @MinerConfig(
            groupProvider = NoErrorGroupProvider.class,
            dataIdProvider = ErrorDataIdProvider.class
    )
    interface ProvideError2 {

        String prop();
    }

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

    @MinerConfig(group = "Arg", dataId = "data")
    interface ArgMiner {

        @MinerConfig("custom1.${customKey1}")
        String custom1();

        @MinerConfig("custom2.${customKey2}")
        String custom2();
    }

    @Data
    public static class MinerContentBean implements ParamsAppliable {

        private String name;

        @Override
        public void applyParams(String[] strings) {
            if (strings.length > 0) this.name = strings[0];
        }
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

    @MinerConfig
    class StoneConcrete {}
}
