package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.core.net.common.AcceptCharset;
import com.github.charlemaznable.core.net.common.ClientProxy;
import com.github.charlemaznable.core.net.common.ClientProxy.ProxyProvider;
import com.github.charlemaznable.core.net.common.ClientSSL;
import com.github.charlemaznable.core.net.common.ClientSSL.HostnameVerifierProvider;
import com.github.charlemaznable.core.net.common.ClientSSL.SSLSocketFactoryProvider;
import com.github.charlemaznable.core.net.common.ClientSSL.X509TrustManagerProvider;
import com.github.charlemaznable.core.net.common.ContentFormat;
import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.core.net.common.DefaultErrorMappingDisabled;
import com.github.charlemaznable.core.net.common.FixedContext;
import com.github.charlemaznable.core.net.common.FixedHeader;
import com.github.charlemaznable.core.net.common.FixedParameter;
import com.github.charlemaznable.core.net.common.FixedPathVar;
import com.github.charlemaznable.core.net.common.FixedValueProvider;
import com.github.charlemaznable.core.net.common.HttpMethod;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.IsolatedConnectionPool;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.Mapping.UrlProvider;
import com.github.charlemaznable.core.net.common.RequestMethod;
import com.github.charlemaznable.core.net.common.StatusError;
import com.github.charlemaznable.core.net.common.StatusErrorMapping;
import com.github.charlemaznable.core.net.common.StatusSeriesErrorMapping;
import com.github.charlemaznable.core.net.ohclient.OhClient;
import com.github.charlemaznable.core.net.ohclient.OhException;
import com.github.charlemaznable.core.net.ohclient.OhReq;
import com.google.common.cache.LoadingCache;
import lombok.val;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
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
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.ohConnectionPool;
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

        this.clientProxy = Elf.checkClientProxy(this.ohClass);
        val clientSSL = Elf.checkClientSSL(this.ohClass);
        if (null != clientSSL) {
            this.sslSocketFactory = Elf.checkSSLSocketFactory(this.ohClass, clientSSL);
            this.x509TrustManager = Elf.checkX509TrustManager(this.ohClass, clientSSL);
            this.hostnameVerifier = Elf.checkHostnameVerifier(this.ohClass, clientSSL);
        }
        this.connectionPool = Elf.checkConnectionPool(this.ohClass);
        this.okHttpClient = Elf.buildOkHttpClient(this);

        this.acceptCharset = Elf.checkAcceptCharset(this.ohClass);
        this.contentFormatter = Elf.checkContentFormatter(this.ohClass);
        this.httpMethod = Elf.checkHttpMethod(this.ohClass);
        this.headers = Elf.checkFixedHeaders(this.ohClass);
        this.pathVars = Elf.checkFixedPathVars(this.ohClass);
        this.parameters = Elf.checkFixedParameters(this.ohClass);
        this.contexts = Elf.checkFixedContexts(this.ohClass);

        this.statusErrorMapping = Elf.checkStatusErrorMapping(this.ohClass);
        this.statusSeriesErrorMapping = Elf.checkStatusSeriesErrorMapping(this.ohClass);
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
            val mapping = findAnnotation(clazz, Mapping.class);
            if (null == mapping) return "";
            val providerClass = mapping.urlProvider();
            return ohSubstitutor.replace(UrlProvider.class == providerClass ?
                    mapping.value() : getBeanOrReflect(providerClass).url(clazz));
        }

        static Proxy checkClientProxy(Class clazz) {
            val clientProxy = findAnnotation(clazz, ClientProxy.class);
            return notNullThen(clientProxy, annotation -> {
                val providerClass = annotation.proxyProvider();
                return ProxyProvider.class == providerClass ?
                        checkBlank(annotation.ip(), () -> null, s -> new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(annotation.ip(), annotation.port())))
                        : getBeanOrReflect(providerClass).proxy(clazz);
            });
        }

        static ClientSSL checkClientSSL(Class clazz) {
            return findAnnotation(clazz, ClientSSL.class);
        }

        static SSLSocketFactory checkSSLSocketFactory(Class clazz, ClientSSL clientSSL) {
            val providerClass = clientSSL.sslSocketFactoryProvider();
            return SSLSocketFactoryProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).sslSocketFactory(clazz);
        }

        static X509TrustManager checkX509TrustManager(Class clazz, ClientSSL clientSSL) {
            val providerClass = clientSSL.x509TrustManagerProvider();
            return X509TrustManagerProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).x509TrustManager(clazz);
        }

        static HostnameVerifier checkHostnameVerifier(Class clazz, ClientSSL clientSSL) {
            val providerClass = clientSSL.hostnameVerifierProvider();
            return HostnameVerifierProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).hostnameVerifier(clazz);
        }

        static ConnectionPool checkConnectionPool(Class clazz) {
            val isolated = findAnnotation(clazz, IsolatedConnectionPool.class);
            return checkNull(isolated, () -> ohConnectionPool, x -> new ConnectionPool());
        }

        static OkHttpClient buildOkHttpClient(OhProxy proxy) {
            return new OhReq().clientProxy(proxy.clientProxy)
                    .sslSocketFactory(proxy.sslSocketFactory)
                    .x509TrustManager(proxy.x509TrustManager)
                    .hostnameVerifier(proxy.hostnameVerifier)
                    .connectionPool(proxy.connectionPool)
                    .buildHttpClient();
        }

        static Charset checkAcceptCharset(Class clazz) {
            val acceptCharset = findAnnotation(clazz, AcceptCharset.class);
            return checkNull(acceptCharset, () -> DEFAULT_ACCEPT_CHARSET,
                    annotation -> Charset.forName(annotation.value()));
        }

        static ContentFormatter checkContentFormatter(Class clazz) {
            val contentFormat = findAnnotation(clazz, ContentFormat.class);
            return checkNull(contentFormat, () -> DEFAULT_CONTENT_FORMATTER,
                    annotation -> getBeanOrReflect(annotation.value()));
        }

        static HttpMethod checkHttpMethod(Class clazz) {
            val requestMethod = findAnnotation(clazz, RequestMethod.class);
            return checkNull(requestMethod, () -> DEFAULT_HTTP_METHOD, RequestMethod::value);
        }

        static List<Pair<String, String>> checkFixedHeaders(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, FixedHeader.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, name));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, String>> checkFixedPathVars(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, FixedPathVar.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, name));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedParameters(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, FixedParameter.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static List<Pair<String, Object>> checkFixedContexts(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, FixedContext.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, name)));
                    }).collect(Collectors.toList());
        }

        static Map<HttpStatus, Class<? extends StatusError>> checkStatusErrorMapping(Class clazz) {
            return newArrayList(findMergedRepeatableAnnotations(clazz, StatusErrorMapping.class)).stream()
                    .collect(Collectors.toMap(StatusErrorMapping::status, StatusErrorMapping::exception));
        }

        static Map<HttpStatus.Series, Class<? extends StatusError>> checkStatusSeriesErrorMapping(Class clazz) {
            val defaultDisabled = findAnnotation(clazz, DefaultErrorMappingDisabled.class);
            Map<HttpStatus.Series, Class<? extends StatusError>> result = checkNull(
                    defaultDisabled, () -> of(HttpStatus.Series.CLIENT_ERROR, StatusError.class,
                            HttpStatus.Series.SERVER_ERROR, StatusError.class), x -> newHashMap());
            result.putAll(newArrayList(findMergedRepeatableAnnotations(clazz, StatusSeriesErrorMapping.class)).stream()
                    .collect(Collectors.toMap(StatusSeriesErrorMapping::statusSeries, StatusSeriesErrorMapping::exception)));
            return result;
        }
    }
}
