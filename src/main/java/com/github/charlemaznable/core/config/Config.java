package com.github.charlemaznable.core.config;

import com.github.charlemaznable.core.config.impl.ConfigBuilder;
import com.github.charlemaznable.core.config.impl.IniConfigable;
import com.github.charlemaznable.core.config.impl.PropertiesConfigable;
import com.github.charlemaznable.core.config.impl.PropsConfigable;
import lombok.val;
import lombok.var;

import java.util.List;
import java.util.Properties;

import static com.github.charlemaznable.core.lang.ClzPath.classResource;
import static com.github.charlemaznable.core.spring.ClzResolver.getResources;

public final class Config {

    private static Configable impl;

    static {
        loadConfigImplementation();
    }

    private Config() {}

    private static void loadConfigImplementation() {
        val defConfig = createConfigable("defconfigdir", "defconfig", null);
        impl = createConfigable("bizconfigdir", "bizconfig", defConfig);
    }

    private static Configable createConfigable(String configKey, String defConfigDir, Configable defConfig) {
        val configBuilder = new ConfigBuilder();
        configBuilder.setDefConfig(defConfig);

        var basePackage = defConfigDir;
        val envURL = classResource("envspace.props");
        if (envURL != null) {
            val envSpaceConfig = new PropsConfigable(envURL);
            basePackage = envSpaceConfig.getStr(configKey, defConfigDir);
            configBuilder.addConfig(envSpaceConfig);
        }

        val propertiesURL = getResources(basePackage, "properties");
        for (val propertyURL : propertiesURL) {
            configBuilder.addConfig(new PropertiesConfigable(propertyURL));
        }
        val propsURL = getResources(basePackage, "props");
        for (val propURL : propsURL) {
            configBuilder.addConfig(new PropsConfigable(propURL));
        }
        val inisURL = getResources(basePackage, "ini");
        for (val iniURL : inisURL) {
            configBuilder.addConfig(new IniConfigable(iniURL));
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

    public static <T> T getBean(String key, Class<T> beanClass) {
        return impl.getBean(key, beanClass);
    }

    public static <T> List<T> getBeans(String key, Class<T> beanClass) {
        return impl.getBeans(key, beanClass);
    }

    /**
     * 提供一个可以获取配置impl的入口,
     * 用于展示出当前配置impl所有配置的结果集,
     */
    public static Configable getConfigImpl() {
        return impl;
    }
}
