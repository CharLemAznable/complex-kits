package com.github.charlemaznable.config;

import com.github.charlemaznable.config.impl.ConfigBuilder;
import com.github.charlemaznable.config.impl.DefConfigSetter;
import com.github.charlemaznable.config.impl.IniConfigable;
import com.github.charlemaznable.config.impl.PropertiesConfigable;
import com.github.charlemaznable.config.impl.PropsConfigable;
import com.github.charlemaznable.config.impl.TableConfigable;
import com.github.charlemaznable.config.utils.ParamsApplyUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import static com.github.charlemaznable.lang.ClzPath.classResource;
import static com.github.charlemaznable.lang.ClzPath.classResources;

public class Config {

    private static Configable impl;

    static {
        loadConfigImplementation();
    }

    private static void loadConfigImplementation() {
        Configable defConfig = createConfigable("defconfigdir", "defconfig", null);
        Configable bizConfig = createConfigable("bizconfigdir", "bizconfig", defConfig);

        // 加载配置系统独立实现类（比如从Redis、Mysql、Oracle等读取配置的具体实现）
        // 要求具体配置类必须实现Configable接口，按照需要实现ParamsAppliable、DefConfigSetter接口
        // 例如：
        // config.implementation=org.n3r.config.impl.RedisConfigable(127.0.0.1,
        // 11211)
        String configImplementation = bizConfig.getStr("config.implementation");
        if (StringUtils.isEmpty(configImplementation)) {
            impl = bizConfig;
            return;
        }

        impl = loadImpl(configImplementation, bizConfig);
        if (impl instanceof DefConfigSetter) { // 设置缺省配置读取对象
            ((DefConfigSetter) impl).setDefConfig(defConfig);
        }
    }

    private static Configable loadImpl(String configImplementation, Configable defConfig) {
        return ParamsApplyUtils.createObject(configImplementation, Configable.class);
    }

    private static Configable createConfigable(String configKey, String defConfigDir, Configable defConfig) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setDefConfig(defConfig);

        String basePackage = defConfigDir;
        URL envURL = classResource("envspace.props");
        if (envURL != null) {
            PropsConfigable envSpaceConfig = new PropsConfigable(envURL);
            basePackage = envSpaceConfig.getStr(configKey, defConfigDir);
            configBuilder.addConfig(envSpaceConfig);
        }

        URL[] propertiesURL = classResources(basePackage, "properties");
        for (URL propertyURL : propertiesURL) {
            configBuilder.addConfig(new PropertiesConfigable(propertyURL));
        }
        URL[] propsURL = classResources(basePackage, "props");
        for (URL propURL : propsURL) {
            configBuilder.addConfig(new PropsConfigable(propURL));
        }
        URL[] inisURL = classResources(basePackage, "ini");
        for (URL iniURL : inisURL) {
            configBuilder.addConfig(new IniConfigable(iniURL));
        }
        URL[] tablesURL = classResources(basePackage, "table");
        for (URL tableURL : tablesURL) {
            configBuilder.addConfig(new TableConfigable(tableURL));
        }

        return configBuilder.buildConfig();
    }

    public static boolean exists(String key) {
        return impl.exists(key);
    }

    public static Properties getProperties() {
        return impl.getProperties();
    }

    public static int getInt(String key) {
        return impl.getInt(key);
    }

    public static long getLong(String key) {
        return impl.getLong(key);
    }

    public static boolean getBool(String key) {
        return impl.getBool(key);
    }

    public static float getFloat(String key) {
        return impl.getFloat(key);
    }

    public static double getDouble(String key) {
        return impl.getDouble(key);
    }

    public static String getStr(String key) {
        return impl.getStr(key);
    }

    public static int getInt(String key, int defaultValue) {
        return impl.getInt(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return impl.getLong(key, defaultValue);
    }

    public static boolean getBool(String key, boolean defaultValue) {
        return impl.getBool(key, defaultValue);
    }

    public static float getFloat(String key, float defaultValue) {
        return impl.getFloat(key, defaultValue);
    }

    public static double getDouble(String key, double defaultValue) {
        return impl.getDouble(key, defaultValue);
    }

    public static String getStr(String key, String defaultValue) {
        return impl.getStr(key, defaultValue);
    }

    public static Configable subset(String prefix) {
        return impl.subset(prefix);
    }

    public static long refreshConfigSet(String prefix) {
        return impl.refreshConfigSet(prefix);
    }

    public static <T> T getBean(String key, Class<T> beanClass) {
        return impl.getBean(key, beanClass);
    }

    public static <T> List<T> getBeans(String key, Class<T> beanClass) {
        return impl.getBeans(key, beanClass);
    }

    /**
     * 提供一个可以获取配置impl的入口,
     * 用于展示出当前配置impl所有配置的结果集,
     * 修改了配置刷新相应配置,免重启服务,免发布.
     */
    public static Configable getConfigImpl() {
        return impl;
    }
}
