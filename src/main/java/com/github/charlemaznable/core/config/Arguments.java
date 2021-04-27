package com.github.charlemaznable.core.config;

import com.github.charlemaznable.core.config.impl.BaseConfigable;
import com.github.charlemaznable.core.config.impl.DefaultConfigable;
import lombok.val;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

public final class Arguments extends BaseConfigable {

    private static final Properties properties = new Properties();

    public static void initial(String... args) {
        val arguments = new ArgumentsWrapper(args);
        properties.clear();

        val optionNames = arguments.getOptionNames();
        for (val optionName : optionNames) {
            val optionValues = arguments.getOptionValues(optionName);
            properties.put(optionName, String.join(",", optionValues));
        }

        val nonOptionArgs = arguments.getNonOptionArgs();
        for (val nonOptionArg : nonOptionArgs) {
            properties.put(nonOptionArg, nonOptionArg);
        }
    }

    public static StringSubstitutor argumentsAsSubstitutor() {
        Map<String, String> argPropsMap = newHashMap();
        val propNames = properties.propertyNames();
        while (propNames.hasMoreElements()) {
            val propName = (String) propNames.nextElement();
            val propValue = properties.getProperty(propName);
            argPropsMap.put(propName, propValue);
        }
        return new StringSubstitutor(argPropsMap);
    }

    @Override
    public boolean exists(String key) {
        return properties.containsKey(key);
    }

    @Override
    public Properties getProperties() {
        val prop = new Properties();
        prop.putAll(properties);
        return prop;
    }

    @Override
    public String getStr(String key) {
        return trim(properties.getProperty(key));
    }

    @Override
    public Configable subset(String prefix) {
        if (isEmpty(prefix)) return new DefaultConfigable(new Properties());

        val prefixMatch = prefix.charAt(prefix.length() - 1) != '.' ? prefix + '.' : prefix;
        val subProps = DefaultConfigable.subProperties(properties, prefixMatch);
        return new DefaultConfigable(subProps);
    }

    private static final class ArgumentsWrapper {

        private final Source source;

        public ArgumentsWrapper(String... args) {
            checkNotNull(args, "Args must not be null");
            this.source = new Source(args);
        }

        public Set<String> getOptionNames() {
            val names = Arrays.asList(this.source.getPropertyNames());
            return Collections.unmodifiableSet(new HashSet<>(names));
        }

        public List<String> getOptionValues(String name) {
            val values = this.source.getOptionValues(name);
            return notNullThen(values, Collections::unmodifiableList);
        }

        public List<String> getNonOptionArgs() {
            return this.source.getNonOptionArgs();
        }
    }

    private static final class Source extends SimpleCommandLinePropertySource {

        Source(String[] args) {
            super(args);
        }

        @Override
        public List<String> getNonOptionArgs() {
            return super.getNonOptionArgs();
        }

        @Override
        public List<String> getOptionValues(@Nonnull String name) {
            return super.getOptionValues(name);
        }
    }
}
