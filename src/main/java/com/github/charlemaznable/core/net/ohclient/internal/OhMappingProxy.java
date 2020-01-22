package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.ohclient.OhMapping;
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
import com.github.charlemaznable.core.net.ohclient.exception.OhException;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedContext;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedHeader;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedParameter;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedPathVar;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedValueProvider;
import com.github.charlemaznable.core.net.ohclient.param.OhStatusMapping;
import com.github.charlemaznable.core.net.ohclient.param.OhStatusSeriesMapping;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.codec.Json.desc;
import static com.github.charlemaznable.core.lang.Condition.checkBlank;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.ohExecutorService;
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.ohSubstitutor;
import static com.github.charlemaznable.core.spring.SpringContext.getBeanOrReflect;
import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public final class OhMappingProxy extends OhRoot {

    Class ohClass;
    Method ohMethod;
    String requestUrl;

    boolean returnFuture; // Future<V>
    boolean returnCollection; // Collection<E>
    boolean returnMap; // Map<K, V>
    boolean returnPair; // Pair<L, R>
    boolean returnTriple; // Triple<L, M, R>
    List<Class> returnTypes;

    OhMappingProxy(Class ohClass, Method ohMethod, OhProxy proxy) {
        this.ohClass = ohClass;
        this.ohMethod = ohMethod;
        this.requestUrl = Elf.checkRequestUrl(this.ohMethod, proxy);

        this.proxy = Elf.checkProxy(this.ohClass, this.ohMethod, proxy);
        val configSSL = Elf.checkConfigSSL(this.ohMethod);
        if (null != configSSL) {
            this.sslSocketFactory = Elf.checkSSLSocketFactory(
                    this.ohClass, this.ohMethod, configSSL);
            this.x509TrustManager = Elf.checkX509TrustManager(
                    this.ohClass, this.ohMethod, configSSL);
            this.hostnameVerifier = Elf.checkHostnameVerifier(
                    this.ohClass, this.ohMethod, configSSL);
        } else {
            this.sslSocketFactory = proxy.sslSocketFactory;
            this.x509TrustManager = proxy.x509TrustManager;
            this.hostnameVerifier = proxy.hostnameVerifier;
        }
        this.okHttpClient = Elf.buildOkHttpClient(this, proxy);

        this.acceptCharset = Elf.checkAcceptCharset(this.ohMethod, proxy);
        this.contentFormat = Elf.checkContentFormat(this.ohMethod, proxy);
        this.requestMethod = Elf.checkRequestMethod(this.ohMethod, proxy);
        this.headers = Elf.checkFixedHeaders(this.ohClass, this.ohMethod, proxy);
        this.pathVars = Elf.checkFixedPathVars(this.ohClass, this.ohMethod, proxy);
        this.parameters = Elf.checkFixedParameters(this.ohClass, this.ohMethod, proxy);
        this.contexts = Elf.checkFixedContexts(this.ohClass, this.ohMethod, proxy);

        this.statusMapping = Elf.checkStatusMapping(this.ohMethod, proxy);
        this.statusSeriesMapping = Elf.checkStatusSeriesMapping(this.ohMethod, proxy);

        processReturnType(this.ohMethod);
    }

    Object execute(Object[] args) {
        if (this.returnFuture) {
            return ohExecutorService.submit(
                    () -> internalExecute(args));
        }
        return internalExecute(args);
    }

    private void processReturnType(Method method) {
        val returnType = method.getReturnType();
        this.returnFuture = Future.class == returnType;
        this.returnCollection = Collection.class.isAssignableFrom(returnType);
        this.returnMap = Map.class.isAssignableFrom(returnType);
        this.returnPair = Pair.class.isAssignableFrom(returnType);
        this.returnTriple = Triple.class.isAssignableFrom(returnType);

        val genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            // 错误的泛型时
            if (this.returnFuture || this.returnCollection ||
                    this.returnMap || this.returnPair || this.returnTriple) {
                // 如返回支持的泛型类型则抛出异常
                throw new OhException("Method return type generic Error");
            } else {
                // 否则以方法返回类型作为实际返回类型
                this.returnTypes = newArrayList(returnType);
                return;
            }
        }

        // 方法返回泛型时
        ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
        var actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (this.returnFuture) {
            // 返回Future类型, 则多处理一层泛型
            val futureTypeArgument = actualTypeArguments[0];
            if (!(futureTypeArgument instanceof ParameterizedType)) {
                this.returnTypes = newArrayList((Class) futureTypeArgument);
                return;
            }

            parameterizedType = (ParameterizedType) futureTypeArgument;
            Class futureTypeClass = (Class) parameterizedType.getRawType();
            this.returnCollection = Collection.class.isAssignableFrom(futureTypeClass);
            this.returnMap = Map.class.isAssignableFrom(futureTypeClass);
            this.returnPair = Pair.class.isAssignableFrom(futureTypeClass);
            this.returnTriple = Triple.class.isAssignableFrom(futureTypeClass);
            actualTypeArguments = parameterizedType.getActualTypeArguments();
        }
        if (this.returnMap) {
            // 返回Map时, 直接解析返回值为Map
            this.returnTypes = newArrayList(Map.class);
            return;
        }
        // 以泛型参数类型作为返回值解析目标类型
        this.returnTypes = newArrayList();
        for (Type actualTypeArgument : actualTypeArguments) {
            this.returnTypes.add((Class) actualTypeArgument);
        }
    }

    @SneakyThrows
    private Object internalExecute(Object[] args) {
        val response = new OhCall(this, args).execute();

        val statusCode = response.code();
        val errorMapping = new ErrorMappingFunction();
        notNullThen(this.statusMapping.get(HttpStatus
                .valueOf(statusCode)), errorMapping);
        notNullThen(this.statusSeriesMapping.get(HttpStatus.Series
                .valueOf(statusCode)), errorMapping);

        val responseBody = response.body();
        val responseObjs = processResponseBody(statusCode, responseBody);
        if (this.returnCollection) {
            val responseObj = responseObjs.get(0);
            if (responseObj instanceof Collection) {
                return newArrayList((Collection) responseObj);
            } else {
                return newArrayList(responseObj);
            }

        } else if (this.returnMap) {
            val responseObj = responseObjs.get(0);
            if (responseObj instanceof Map) {
                return newHashMap((Map) responseObj);
            } else {
                return desc(responseObj);
            }

        } else if (this.returnPair) {
            return Pair.of(responseObjs.get(0), responseObjs.get(1));

        } else if (this.returnTriple) {
            return Triple.of(responseObjs.get(0), responseObjs.get(1), responseObjs.get(2));

        } else {
            return responseObjs.get(0);
        }
    }

    private List<Object> processResponseBody(int statusCode, ResponseBody responseBody) {
        List<Object> returnValues = newArrayList();
        for (val returnType : this.returnTypes) {
            returnValues.add(processReturnTypeValue(statusCode, responseBody, returnType));
        }
        return returnValues;
    }

    private Object processReturnTypeValue(int statusCode, ResponseBody responseBody, Class returnType) {
        if (void.class == returnType || Void.class == returnType) {
            return null;
        } else if (int.class == returnType || Integer.class == returnType) {
            return statusCode;
        } else if (HttpStatus.class == returnType) {
            return HttpStatus.valueOf(statusCode);
        } else if (HttpStatus.Series.class == returnType) {
            return HttpStatus.Series.valueOf(statusCode);
        } else if (boolean.class == returnType || Boolean.class == returnType) {
            return HttpStatus.valueOf(statusCode).is2xxSuccessful();
        } else if (ResponseBody.class.isAssignableFrom(returnType)) {
            return responseBody;
        } else if (InputStream.class == returnType) {
            return notNullThen(responseBody, ResponseBodyExtractor::byteStream);
        } else if (BufferedSource.class.isAssignableFrom(returnType)) {
            return (notNullThen(responseBody, ResponseBodyExtractor::source));
        } else if (byte[].class == returnType) {
            return notNullThen(responseBody, ResponseBodyExtractor::bytes);
        } else if (Reader.class.isAssignableFrom(returnType)) {
            return notNullThen(responseBody, ResponseBodyExtractor::charStream);
        } else if (String.class == returnType) {
            return notNullThen(responseBody, ResponseBodyExtractor::string);
        } else {
            return notNullThen(responseBody, body ->
                    ResponseBodyExtractor.object(body, returnType));
        }
    }

    static class Elf {

        private Elf() {
            throw new UnsupportedOperationException();
        }

        static String checkRequestUrl(Method method, OhProxy proxy) {
            val ohMapping = findAnnotation(method, OhMapping.class);
            val url = checkNull(ohMapping, method::getName,
                    mapping -> ohSubstitutor.replace(mapping.value()));
            if (isBlank(url)) return proxy.baseUrl;
            if (isBlank(proxy.baseUrl)) return url;
            return removeEnd(proxy.baseUrl, "/") + prependIfMissing(url, "/");
        }

        static Proxy checkProxy(Class clazz, Method method, OhProxy proxy) {
            val configProxy = findAnnotation(method, OhConfigProxy.class);
            return checkNull(configProxy, () -> proxy.proxy, annotation -> {
                val providerClass = annotation.proxyProvider();
                return ProxyProvider.class == providerClass ?
                        checkBlank(annotation.ip(), () -> null, s -> new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(annotation.ip(), annotation.port())))
                        : getBeanOrReflect(providerClass).proxy(clazz, method);
            });
        }

        static OhConfigSSL checkConfigSSL(Method method) {
            return findAnnotation(method, OhConfigSSL.class);
        }

        static SSLSocketFactory checkSSLSocketFactory(Class clazz, Method method,
                                                      OhConfigSSL configSSL) {
            val providerClass = configSSL.sslSocketFactoryProvider();
            return SSLSocketFactoryProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).sslSocketFactory(clazz, method);
        }

        static X509TrustManager checkX509TrustManager(Class clazz, Method method,
                                                      OhConfigSSL configSSL) {
            val providerClass = configSSL.x509TrustManagerProvider();
            return X509TrustManagerProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).x509TrustManager(clazz, method);
        }

        static HostnameVerifier checkHostnameVerifier(Class clazz, Method method,
                                                      OhConfigSSL configSSL) {
            val providerClass = configSSL.hostnameVerifierProvider();
            return HostnameVerifierProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).hostnameVerifier(clazz, method);
        }

        @SuppressWarnings("deprecation")
        static OkHttpClient buildOkHttpClient(OhMappingProxy mappingProxy, OhProxy proxy) {
            val sameProxy = mappingProxy.proxy == proxy.proxy;
            val sameSSLSocketFactory = mappingProxy.sslSocketFactory == proxy.sslSocketFactory;
            val sameX509TrustManager = mappingProxy.x509TrustManager == proxy.x509TrustManager;
            val sameHostnameVerifier = mappingProxy.hostnameVerifier == proxy.hostnameVerifier;
            if (sameProxy && sameSSLSocketFactory && sameX509TrustManager
                    && sameHostnameVerifier) return proxy.okHttpClient;

            val httpClientBuilder = new OkHttpClient.Builder().proxy(mappingProxy.proxy);
            notNullThen(mappingProxy.sslSocketFactory, sslSocketFactory -> checkNull(mappingProxy.x509TrustManager,
                    () -> httpClientBuilder.sslSocketFactory(mappingProxy.sslSocketFactory),
                    xx -> httpClientBuilder.sslSocketFactory(mappingProxy.sslSocketFactory, mappingProxy.x509TrustManager)));
            notNullThen(mappingProxy.hostnameVerifier, httpClientBuilder::hostnameVerifier);
            return httpClientBuilder.build();
        }

        static Charset checkAcceptCharset(Method method, OhProxy proxy) {
            val configAcceptCharset = findAnnotation(method, OhConfigAcceptCharset.class);
            return checkNull(configAcceptCharset, () -> proxy.acceptCharset,
                    annotation -> Charset.forName(annotation.value()));
        }

        static ContentFormat checkContentFormat(Method method, OhProxy proxy) {
            val configContentType = findAnnotation(method, OhConfigContentFormat.class);
            return checkNull(configContentType, () -> proxy.contentFormat,
                    annotation -> getBeanOrReflect(annotation.value()));
        }

        static RequestMethod checkRequestMethod(Method method, OhProxy proxy) {
            val configRequestMethod = findAnnotation(method, OhConfigRequestMethod.class);
            return checkNull(configRequestMethod, () -> proxy.requestMethod,
                    OhConfigRequestMethod::value);
        }

        static List<Pair<String, String>> checkFixedHeaders(Class clazz, Method method, OhProxy proxy) {
            val result = newArrayList(proxy.headers);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, OhFixedHeader.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val providerClass = an.valueProvider();
                        return Pair.of(an.name(), OhFixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, method));
                    }).collect(Collectors.toList()));
            return result;
        }

        static List<Pair<String, String>> checkFixedPathVars(Class clazz, Method method, OhProxy proxy) {
            val result = newArrayList(proxy.pathVars);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, OhFixedPathVar.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val providerClass = an.valueProvider();
                        return Pair.of(an.name(), OhFixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, method));
                    }).collect(Collectors.toList()));
            return result;
        }

        static List<Pair<String, Object>> checkFixedParameters(Class clazz, Method method, OhProxy proxy) {
            val result = newArrayList(proxy.parameters);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, OhFixedParameter.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val providerClass = an.valueProvider();
                        return Pair.of(an.name(), (Object) (OhFixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, method)));
                    }).collect(Collectors.toList()));
            return result;
        }

        static List<Pair<String, Object>> checkFixedContexts(Class clazz, Method method, OhProxy proxy) {
            val result = newArrayList(proxy.contexts);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, OhFixedContext.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val providerClass = an.valueProvider();
                        return Pair.of(an.name(), (Object) (OhFixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, method)));
                    }).collect(Collectors.toList()));
            return result;
        }

        static Map<HttpStatus, Class<? extends RuntimeException>>
        checkStatusMapping(Method method, OhProxy proxy) {
            val result = newHashMap(proxy.statusMapping);
            result.putAll(newArrayList(findMergedRepeatableAnnotations(method, OhStatusMapping.class)).stream()
                    .collect(Collectors.toMap(OhStatusMapping::status, OhStatusMapping::exception)));
            return result;
        }

        static Map<HttpStatus.Series, Class<? extends RuntimeException>>
        checkStatusSeriesMapping(Method method, OhProxy proxy) {
            val result = newHashMap(proxy.statusSeriesMapping);
            result.putAll(newArrayList(findMergedRepeatableAnnotations(method, OhStatusSeriesMapping.class)).stream()
                    .collect(Collectors.toMap(OhStatusSeriesMapping::statusSeries, OhStatusSeriesMapping::exception)));
            return result;
        }
    }
}
