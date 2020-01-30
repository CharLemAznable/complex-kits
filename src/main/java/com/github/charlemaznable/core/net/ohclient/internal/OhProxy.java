package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.core.net.ohclient.OhClient;
import com.github.charlemaznable.core.net.ohclient.OhMapping;
import com.github.charlemaznable.core.net.ohclient.OhMapping.UrlProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigAcceptCharset;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.ContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigProxy;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigProxy.ProxyProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigRequestMethod;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigSSL;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigSSL.HostnameVerifierProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigSSL.SSLSocketFactoryProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigSSL.X509TrustManagerProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhDefaultErrorMappingDisabled;
import com.github.charlemaznable.core.net.ohclient.exception.OhError;
import com.github.charlemaznable.core.net.ohclient.exception.OhException;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedContext;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedHeader;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedParameter;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedPathVar;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedValueProvider;
import com.github.charlemaznable.core.net.ohclient.param.OhStatusMapping;
import com.github.charlemaznable.core.net.ohclient.param.OhStatusSeriesMapping;
import com.google.common.cache.LoadingCache;
import lombok.val;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.lang.Condition.checkBlank;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMAT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_REQUEST_METHOD;
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.ohSubstitutor;
import static com.github.charlemaznable.core.spring.SpringContext.getBeanOrReflect;
import static com.google.common.cache.CacheLoader.from;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class OhProxy extends OhRoot implements MethodInterceptor {

    Class ohClass;
    String baseUrl;

    LoadingCache<Method, OhMappingProxy> ohMappingProxyCache
            = LoadingCachee.simpleCache(from(this::loadMappingProxy));

    public OhProxy(Class ohClass) {
        this.ohClass = ohClass;
        Elf.checkOhClient(this.ohClass);
        this.baseUrl = Elf.checkBaseUrl(this.ohClass);

        this.proxy = Elf.checkProxy(this.ohClass);
        val configSSL = Elf.checkConfigSSL(this.ohClass);
        if (null != configSSL) {
            this.sslSocketFactory = Elf.checkSSLSocketFactory(this.ohClass, configSSL);
            this.x509TrustManager = Elf.checkX509TrustManager(this.ohClass, configSSL);
            this.hostnameVerifier = Elf.checkHostnameVerifier(this.ohClass, configSSL);
        }
        this.okHttpClient = Elf.buildOkHttpClient(this);

        this.acceptCharset = Elf.checkAcceptCharset(this.ohClass);
        this.contentFormat = Elf.checkContentFormat(this.ohClass);
        this.requestMethod = Elf.checkRequestMethod(this.ohClass);
        this.headers = Elf.checkFixedHeaders(this.ohClass);
        this.pathVars = Elf.checkFixedPathVars(this.ohClass);
        this.parameters = Elf.checkFixedParameters(this.ohClass);
        this.contexts = Elf.checkFixedContexts(this.ohClass);

        this.statusMapping = Elf.checkStatusMapping(this.ohClass);
        this.statusSeriesMapping = Elf.checkStatusSeriesMapping(this.ohClass);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args,
                            MethodProxy methodProxy) throws Throwable {
        if (method.getDeclaringClass().equals(OhDummy.class)) {
            return methodProxy.invokeSuper(o, args);
        }

        val mappingProxy = LoadingCachee.get(
                this.ohMappingProxyCache, method);
        return mappingProxy.execute(args);
    }

    private OhMappingProxy loadMappingProxy(Method method) {
        return new OhMappingProxy(this.ohClass, method, this);
    }

    static class Elf {

        private Elf() {
            throw new UnsupportedOperationException();
        }

        static void checkOhClient(Class clazz) {
            checkNotNull(getAnnotation(clazz, OhClient.class),
                    new OhException(clazz.getName() + " has no OhClient annotation"));
        }

        static String checkBaseUrl(Class clazz) {
            val ohMapping = findAnnotation(clazz, OhMapping.class);
            if (null == ohMapping) return "";
            val providerClass = ohMapping.urlProvider();
            return ohSubstitutor.replace(UrlProvider.class == providerClass ?
                    ohMapping.value() : getBeanOrReflect(providerClass).url(clazz));
        }

        static Proxy checkProxy(Class clazz) {
            val configProxy = findAnnotation(clazz, OhConfigProxy.class);
            return notNullThen(configProxy, annotation -> {
                val providerClass = annotation.proxyProvider();
                return ProxyProvider.class == providerClass ?
                        checkBlank(annotation.ip(), () -> null, s -> new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(annotation.ip(), annotation.port())))
                        : getBeanOrReflect(providerClass).proxy(clazz);
            });
        }

        static OhConfigSSL checkConfigSSL(Class clazz) {
            return findAnnotation(clazz, OhConfigSSL.class);
        }

        static SSLSocketFactory checkSSLSocketFactory(Class clazz, OhConfigSSL configSSL) {
            val providerClass = configSSL.sslSocketFactoryProvider();
            return SSLSocketFactoryProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).sslSocketFactory(clazz);
        }

        static X509TrustManager checkX509TrustManager(Class clazz, OhConfigSSL configSSL) {
            val providerClass = configSSL.x509TrustManagerProvider();
            return X509TrustManagerProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).x509TrustManager(clazz);
        }

        static HostnameVerifier checkHostnameVerifier(Class clazz, OhConfigSSL configSSL) {
            val providerClass = configSSL.hostnameVerifierProvider();
            return HostnameVerifierProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).hostnameVerifier(clazz);
        }

        @SuppressWarnings("deprecation")
        static OkHttpClient buildOkHttpClient(OhProxy proxy) {
            val httpClientBuilder = new OkHttpClient.Builder().proxy(proxy.proxy);
            notNullThen(proxy.sslSocketFactory, xx -> checkNull(proxy.x509TrustManager,
                    () -> httpClientBuilder.sslSocketFactory(proxy.sslSocketFactory),
                    yy -> httpClientBuilder.sslSocketFactory(proxy.sslSocketFactory, proxy.x509TrustManager)));
            notNullThen(proxy.hostnameVerifier, httpClientBuilder::hostnameVerifier);
            return httpClientBuilder.build();
        }

        static Charset checkAcceptCharset(Class clazz) {
            val configAcceptCharset = findAnnotation(clazz, OhConfigAcceptCharset.class);
            return checkNull(configAcceptCharset, () -> DEFAULT_ACCEPT_CHARSET,
                    annotation -> Charset.forName(annotation.value()));
        }

        static ContentFormat checkContentFormat(Class clazz) {
            val configContentFormat = findAnnotation(clazz, OhConfigContentFormat.class);
            return checkNull(configContentFormat, () -> DEFAULT_CONTENT_FORMAT,
                    annotation -> getBeanOrReflect(annotation.value()));
        }

        static RequestMethod checkRequestMethod(Class clazz) {
            val configRequestMethod = findAnnotation(clazz, OhConfigRequestMethod.class);
            return checkNull(configRequestMethod, () -> DEFAULT_REQUEST_METHOD,
                    OhConfigRequestMethod::value);
        }

        static List<Pair<String, String>> checkFixedHeaders(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, OhFixedHeader.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, OhFixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, name));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, String>> checkFixedPathVars(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, OhFixedPathVar.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, OhFixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, name));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedParameters(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, OhFixedParameter.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (OhFixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedContexts(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, OhFixedContext.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (OhFixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static Map<HttpStatus, Class<? extends OhError>> checkStatusMapping(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, OhStatusMapping.class)).stream()
                    .collect(Collectors.toMap(OhStatusMapping::status, OhStatusMapping::exception));
        }

        static Map<HttpStatus.Series, Class<? extends OhError>> checkStatusSeriesMapping(Class clazz) {
            val disabled = findAnnotation(clazz, OhDefaultErrorMappingDisabled.class);
            Map<HttpStatus.Series, Class<? extends OhError>> result = checkNull(
                    disabled, () -> of(HttpStatus.Series.CLIENT_ERROR, OhError.class,
                            HttpStatus.Series.SERVER_ERROR, OhError.class), x -> newHashMap());
            result.putAll(newArrayList(findMergedRepeatableAnnotations(clazz, OhStatusSeriesMapping.class)).stream()
                    .collect(Collectors.toMap(OhStatusSeriesMapping::statusSeries, OhStatusSeriesMapping::exception)));
            return result;
        }
    }
}
