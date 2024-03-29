package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.core.net.common.AcceptCharset;
import com.github.charlemaznable.core.net.common.ContentFormat;
import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.core.net.common.DefaultFallbackDisabled;
import com.github.charlemaznable.core.net.common.ExtraUrlQuery;
import com.github.charlemaznable.core.net.common.ExtraUrlQuery.ExtraUrlQueryBuilder;
import com.github.charlemaznable.core.net.common.FallbackFunction;
import com.github.charlemaznable.core.net.common.FixedContext;
import com.github.charlemaznable.core.net.common.FixedHeader;
import com.github.charlemaznable.core.net.common.FixedParameter;
import com.github.charlemaznable.core.net.common.FixedPathVar;
import com.github.charlemaznable.core.net.common.FixedValueProvider;
import com.github.charlemaznable.core.net.common.HttpMethod;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.Mapping.UrlProvider;
import com.github.charlemaznable.core.net.common.RequestMethod;
import com.github.charlemaznable.core.net.common.ResponseParse;
import com.github.charlemaznable.core.net.common.ResponseParse.ResponseParser;
import com.github.charlemaznable.core.net.common.StatusErrorThrower;
import com.github.charlemaznable.core.net.common.StatusFallback;
import com.github.charlemaznable.core.net.common.StatusSeriesFallback;
import com.github.charlemaznable.core.net.ohclient.OhClient;
import com.github.charlemaznable.core.net.ohclient.OhException;
import com.github.charlemaznable.core.net.ohclient.OhReq;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientInterceptor;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientInterceptor.InterceptorProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientLoggingLevel;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientLoggingLevel.LoggingLevelProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientProxy;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientProxy.ProxyProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientSSL;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientSSL.HostnameVerifierProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientSSL.SSLSocketFactoryProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientSSL.X509TrustManagerProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientTimeout;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientTimeout.TimeoutProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.IsolatedConnectionPool;
import com.google.common.cache.LoadingCache;
import lombok.val;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.commons.lang3.tuple.Pair;

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
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_HTTP_METHOD;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_LOGGING_LEVEL;
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.ohConnectionPool;
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.substitute;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class OhProxy extends OhRoot implements MethodInterceptor {

    Class ohClass;
    Factory factory;
    String baseUrl;

    LoadingCache<Method, OhMappingProxy> ohMappingProxyCache
            = LoadingCachee.simpleCache(from(this::loadMappingProxy));

    public OhProxy(Class ohClass, Factory factory) {
        this.ohClass = ohClass;
        this.factory = factory;
        Elf.checkOhClient(this.ohClass);
        this.baseUrl = Elf.checkBaseUrl(this.ohClass, this.factory);

        this.clientProxy = Elf.checkClientProxy(this.ohClass, this.factory);
        val clientSSL = Elf.checkClientSSL(this.ohClass);
        if (nonNull(clientSSL)) {
            this.sslSocketFactory = Elf.checkSSLSocketFactory(
                    this.ohClass, this.factory, clientSSL);
            this.x509TrustManager = Elf.checkX509TrustManager(
                    this.ohClass, this.factory, clientSSL);
            this.hostnameVerifier = Elf.checkHostnameVerifier(
                    this.ohClass, this.factory, clientSSL);
        }
        this.connectionPool = Elf.checkConnectionPool(this.ohClass);
        val clientTimeout = Elf.checkClientTimeout(this.ohClass);
        if (nonNull(clientTimeout)) {
            this.callTimeout = Elf.checkCallTimeout(
                    this.ohClass, this.factory, clientTimeout);
            this.connectTimeout = Elf.checkConnectTimeout(
                    this.ohClass, this.factory, clientTimeout);
            this.readTimeout = Elf.checkReadTimeout(
                    this.ohClass, this.factory, clientTimeout);
            this.writeTimeout = Elf.checkWriteTimeout(
                    this.ohClass, this.factory, clientTimeout);
        }
        this.interceptors = Elf.checkClientInterceptors(this.ohClass, this.factory);
        this.loggingLevel = Elf.checkClientLoggingLevel(this.ohClass, this.factory);
        this.okHttpClient = Elf.buildOkHttpClient(this);

        this.acceptCharset = Elf.checkAcceptCharset(this.ohClass);
        this.contentFormatter = Elf.checkContentFormatter(this.ohClass, this.factory);
        this.httpMethod = Elf.checkHttpMethod(this.ohClass);
        this.headers = Elf.checkFixedHeaders(this.ohClass, this.factory);
        this.pathVars = Elf.checkFixedPathVars(this.ohClass, this.factory);
        this.parameters = Elf.checkFixedParameters(this.ohClass, this.factory);
        this.contexts = Elf.checkFixedContexts(this.ohClass, this.factory);

        this.statusFallbackMapping = Elf.checkStatusFallbackMapping(this.ohClass);
        this.statusSeriesFallbackMapping = Elf.checkStatusSeriesFallbackMapping(this.ohClass);

        this.responseParser = Elf.checkResponseParser(this.ohClass, this.factory);

        this.extraUrlQueryBuilder = Elf.checkExtraUrlQueryBuilder(this.ohClass, this.factory);
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
        return new OhMappingProxy(this.ohClass, method, this.factory, this);
    }

    static class Elf {

        private Elf() {
            throw new UnsupportedOperationException();
        }

        static void checkOhClient(Class clazz) {
            checkNotNull(getAnnotation(clazz, OhClient.class),
                    new OhException(clazz.getName() + " has no OhClient annotation"));
        }

        static String checkBaseUrl(Class clazz, Factory factory) {
            val mapping = findAnnotation(clazz, Mapping.class);
            if (isNull(mapping)) return "";
            val providerClass = mapping.urlProvider();
            return substitute(UrlProvider.class == providerClass ? mapping.value()
                    : FactoryContext.apply(factory, providerClass, p -> p.url(clazz)));
        }

        static Proxy checkClientProxy(Class clazz, Factory factory) {
            val clientProxy = findAnnotation(clazz, ClientProxy.class);
            return notNullThen(clientProxy, annotation -> {
                val providerClass = annotation.proxyProvider();
                if (ProxyProvider.class == providerClass) {
                    return checkBlank(annotation.host(), () -> null,
                            xx -> new Proxy(annotation.type(), new InetSocketAddress(
                                    annotation.host(), annotation.port())));
                }
                return FactoryContext.apply(factory, providerClass, p -> p.proxy(clazz));
            });
        }

        static ClientSSL checkClientSSL(Class clazz) {
            return findAnnotation(clazz, ClientSSL.class);
        }

        static SSLSocketFactory checkSSLSocketFactory(
                Class clazz, Factory factory, ClientSSL clientSSL) {
            val providerClass = clientSSL.sslSocketFactoryProvider();
            if (SSLSocketFactoryProvider.class == providerClass) {
                val factoryClass = clientSSL.sslSocketFactory();
                return SSLSocketFactory.class == factoryClass ? null
                        : FactoryContext.build(factory, factoryClass);
            }
            return FactoryContext.apply(factory, providerClass,
                    p -> p.sslSocketFactory(clazz));
        }

        static X509TrustManager checkX509TrustManager(
                Class clazz, Factory factory, ClientSSL clientSSL) {
            val providerClass = clientSSL.x509TrustManagerProvider();
            if (X509TrustManagerProvider.class == providerClass) {
                val managerClass = clientSSL.x509TrustManager();
                return X509TrustManager.class == managerClass ? null
                        : FactoryContext.build(factory, managerClass);
            }
            return FactoryContext.apply(factory, providerClass,
                    p -> p.x509TrustManager(clazz));
        }

        static HostnameVerifier checkHostnameVerifier(
                Class clazz, Factory factory, ClientSSL clientSSL) {
            val providerClass = clientSSL.hostnameVerifierProvider();
            if (HostnameVerifierProvider.class == providerClass) {
                val verifierClass = clientSSL.hostnameVerifier();
                return HostnameVerifier.class == verifierClass ? null
                        : FactoryContext.build(factory, verifierClass);
            }
            return FactoryContext.apply(factory, providerClass,
                    p -> p.hostnameVerifier(clazz));
        }

        static ConnectionPool checkConnectionPool(Class clazz) {
            val isolated = findAnnotation(clazz, IsolatedConnectionPool.class);
            return checkNull(isolated, () -> ohConnectionPool, x -> new ConnectionPool());
        }

        static ClientTimeout checkClientTimeout(Class clazz) {
            return findAnnotation(clazz, ClientTimeout.class);
        }

        static long checkCallTimeout(
                Class clazz, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.callTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.callTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz));
        }

        static long checkConnectTimeout(
                Class clazz, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.connectTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.connectTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz));
        }

        static long checkReadTimeout(
                Class clazz, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.readTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.readTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz));
        }

        static long checkWriteTimeout(
                Class clazz, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.writeTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.writeTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz));
        }

        static List<Interceptor> checkClientInterceptors(Class clazz, Factory factory) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, ClientInterceptor.class))
                    .stream().filter(annotation -> Interceptor.class != annotation.value()
                            || InterceptorProvider.class != annotation.provider())
                    .map(annotation -> {
                        val providerClass = annotation.provider();
                        if (InterceptorProvider.class == providerClass) {
                            return FactoryContext.build(factory, annotation.value());
                        }
                        return FactoryContext.apply(factory, providerClass, p -> p.interceptor(clazz));
                    }).collect(Collectors.toList());
        }

        static Level checkClientLoggingLevel(Class clazz, Factory factory) {
            val clientLoggingLevel = findAnnotation(clazz, ClientLoggingLevel.class);
            if (isNull(clientLoggingLevel)) return DEFAULT_LOGGING_LEVEL;
            val providerClass = clientLoggingLevel.provider();
            return LoggingLevelProvider.class == providerClass ? clientLoggingLevel.value()
                    : FactoryContext.apply(factory, providerClass, p -> p.level(clazz));
        }

        static OkHttpClient buildOkHttpClient(OhProxy proxy) {
            return new OhReq().clientProxy(proxy.clientProxy)
                    .sslSocketFactory(proxy.sslSocketFactory)
                    .x509TrustManager(proxy.x509TrustManager)
                    .hostnameVerifier(proxy.hostnameVerifier)
                    .connectionPool(proxy.connectionPool)
                    .callTimeout(proxy.callTimeout)
                    .connectTimeout(proxy.connectTimeout)
                    .readTimeout(proxy.readTimeout)
                    .writeTimeout(proxy.writeTimeout)
                    .addInterceptors(proxy.interceptors)
                    .loggingLevel(proxy.loggingLevel)
                    .buildHttpClient();
        }

        static Charset checkAcceptCharset(Class clazz) {
            val acceptCharset = findAnnotation(clazz, AcceptCharset.class);
            return checkNull(acceptCharset, () -> DEFAULT_ACCEPT_CHARSET,
                    annotation -> Charset.forName(annotation.value()));
        }

        static ContentFormatter checkContentFormatter(Class clazz, Factory factory) {
            val contentFormat = findAnnotation(clazz, ContentFormat.class);
            return checkNull(contentFormat, () -> DEFAULT_CONTENT_FORMATTER,
                    annotation -> FactoryContext.build(factory, annotation.value()));
        }

        static HttpMethod checkHttpMethod(Class clazz) {
            val requestMethod = findAnnotation(clazz, RequestMethod.class);
            return checkNull(requestMethod, () -> DEFAULT_HTTP_METHOD, RequestMethod::value);
        }

        static List<Pair<String, String>> checkFixedHeaders(Class clazz, Factory factory) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, FixedHeader.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, String>> checkFixedPathVars(Class clazz, Factory factory) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, FixedPathVar.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedParameters(Class clazz, Factory factory) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, FixedParameter.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name))));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedContexts(Class clazz, Factory factory) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, FixedContext.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, name))));
                    }).collect(Collectors.toList());
        }

        static Map<HttpStatus, Class<? extends FallbackFunction>>
        checkStatusFallbackMapping(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(
                    clazz, StatusFallback.class)).stream()
                    .collect(Collectors.toMap(StatusFallback::status,
                            StatusFallback::fallback));
        }

        static Map<HttpStatus.Series, Class<? extends FallbackFunction>>
        checkStatusSeriesFallbackMapping(Class clazz) {
            val defaultDisabled = findAnnotation(clazz, DefaultFallbackDisabled.class);
            Map<HttpStatus.Series, Class<? extends FallbackFunction>> result = checkNull(
                    defaultDisabled, () -> of(HttpStatus.Series.CLIENT_ERROR, StatusErrorThrower.class,
                            HttpStatus.Series.SERVER_ERROR, StatusErrorThrower.class), x -> newHashMap());
            result.putAll(newArrayList(findMergedRepeatableAnnotations(clazz,
                    StatusSeriesFallback.class)).stream()
                    .collect(Collectors.toMap(StatusSeriesFallback::statusSeries,
                            StatusSeriesFallback::fallback)));
            return result;
        }

        static ResponseParser checkResponseParser(Class clazz, Factory factory) {
            val responseParse = findAnnotation(clazz, ResponseParse.class);
            return checkNull(responseParse, () -> null, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }

        static ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(Class clazz, Factory factory) {
            val extraUrlQuery = findAnnotation(clazz, ExtraUrlQuery.class);
            return checkNull(extraUrlQuery, () -> null, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }
    }
}
