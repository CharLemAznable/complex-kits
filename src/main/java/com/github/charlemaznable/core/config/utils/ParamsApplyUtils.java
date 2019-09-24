package com.github.charlemaznable.core.config.utils;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joor.Reflect;

import java.util.List;
import java.util.regex.Pattern;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Str.isEmpty;
import static com.github.charlemaznable.core.lang.Str.substrInQuotes;
import static com.google.common.collect.Iterables.toArray;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
public class ParamsApplyUtils {

    private static final Pattern paramParams = compile(
            "\\w+([.$]\\w+)*\\s*(\\(\\s*[.\\w]+\\s*(,\\s*[.\\w]+\\s*)*\\))?");

    /*
     * 根据形如com.ailk.xxx.yyy(a123,b23)的字符串，生成对象。
     * 如果该对象实现ExtraInfoSetter接口，则将括弧中的额外信息设置到对象中。
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> createObjects(String propertyValue, Class<? super T> cls) {
        List<T> lst = newArrayList();
        if (isEmpty(propertyValue)) return lst;

        val matcher = paramParams.matcher(propertyValue);
        val splitter = Splitter.on(',').trimResults();

        while (matcher.find()) {
            val group = matcher.group().trim();
            val posBrace = group.indexOf('(');
            val functor = posBrace < 0 ? group : group.substring(0, posBrace);
            val obj = Reflect.onClass(trim(functor)).create().get();
            if (!cls.isInstance(obj)) {
                log.warn("{} can not instantized to {}", functor, cls.getName());
                continue;
            }

            lst.add((T) obj);

            if (obj instanceof ParamsAppliable)
                ((ParamsAppliable) obj).applyParams(posBrace <= 0 ? new String[]{}
                        : toArray(splitter.split(substrInQuotes(group, '(', posBrace)), String.class));
        }

        return lst;
    }

    public static <T> T createObject(String propertyValue, Class<? super T> cls) {
        List<T> objects = createObjects(propertyValue, cls);
        return objects.size() > 0 ? objects.get(0) : null;
    }
}