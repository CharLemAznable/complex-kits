package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.net.common.AcceptCharset;
import com.github.charlemaznable.core.net.common.CncResponse;
import com.github.charlemaznable.core.net.common.ContentFormat;
import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.core.net.common.ExtraUrlQuery;
import com.github.charlemaznable.core.net.common.ExtraUrlQuery.ExtraUrlQueryBuilder;
import com.github.charlemaznable.core.net.common.FallbackFunction;
import com.github.charlemaznable.core.net.common.FallbackFunction.Response;
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
import com.github.charlemaznable.core.net.common.StatusFallback;
import com.github.charlemaznable.core.net.common.StatusSeriesFallback;
import com.github.charlemaznable.core.net.ohclient.OhException;
import com.github.charlemaznable.core.net.ohclient.OhReq;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientInterceptor;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientInterceptor.InterceptorProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientInterceptorCleanup;
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
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okio.BufferedSource;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
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
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.ohExecutorService;
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.substitute;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public final class OhMappingProxy extends OhRoot {

    private static final String RETURN_GENERIC_ERROR = "Method return type generic Error";

    Class ohClass;
    Method ohMethod;
    Factory factory;
    String requestUrl;

    boolean returnFuture; // Future<V>
    boolean returnCollection; // Collection<E>
    boolean returnMap; // Map<K, V>
    boolean returnPair; // Pair<L, R>
    boolean returnTriple; // Triple<L, M, R>
    List<Class> returnTypes;

    OhMappingProxy(Class ohClass, Method ohMethod,
                   Factory factory, OhProxy proxy) {
        this.ohClass = ohClass;
        this.ohMethod = ohMethod;
        this.factory = factory;
        this.requestUrl = Elf.checkRequestUrl(this.ohClass,
                this.ohMethod, this.factory, proxy);

        this.clientProxy = Elf.checkClientProxy(
                this.ohClass, this.ohMethod, this.factory, proxy);
        val clientSSL = Elf.checkClientSSL(this.ohMethod);
        if (nonNull(clientSSL)) {
            this.sslSocketFactory = Elf.checkSSLSocketFactory(
                    this.ohClass, this.ohMethod, this.factory, clientSSL);
            this.x509TrustManager = Elf.checkX509TrustManager(
                    this.ohClass, this.ohMethod, this.factory, clientSSL);
            this.hostnameVerifier = Elf.checkHostnameVerifier(
                    this.ohClass, this.ohMethod, this.factory, clientSSL);
        } else {
            this.sslSocketFactory = proxy.sslSocketFactory;
            this.x509TrustManager = proxy.x509TrustManager;
            this.hostnameVerifier = proxy.hostnameVerifier;
        }
        this.connectionPool = Elf.checkConnectionPool(this.ohMethod, proxy);
        val clientTimeout = Elf.checkClientTimeout(this.ohMethod);
        if (nonNull(clientTimeout)) {
            this.callTimeout = Elf.checkCallTimeout(
                    this.ohClass, this.ohMethod, this.factory, clientTimeout);
            this.connectTimeout = Elf.checkConnectTimeout(
                    this.ohClass, this.ohMethod, this.factory, clientTimeout);
            this.readTimeout = Elf.checkReadTimeout(
                    this.ohClass, this.ohMethod, this.factory, clientTimeout);
            this.writeTimeout = Elf.checkWriteTimeout(
                    this.ohClass, this.ohMethod, this.factory, clientTimeout);
        } else {
            this.callTimeout = proxy.callTimeout;
            this.connectTimeout = proxy.connectTimeout;
            this.readTimeout = proxy.readTimeout;
            this.writeTimeout = proxy.writeTimeout;
        }
        this.interceptors = Elf.checkClientInterceptors(
                this.ohClass, this.ohMethod, this.factory, proxy);
        this.loggingLevel = Elf.checkClientLoggingLevel(
                this.ohClass, this.ohMethod, this.factory, proxy);
        this.okHttpClient = Elf.buildOkHttpClient(this, proxy);

        this.acceptCharset = Elf.checkAcceptCharset(this.ohMethod, proxy);
        this.contentFormatter = Elf.checkContentFormatter(
                this.ohMethod, this.factory, proxy);
        this.httpMethod = Elf.checkHttpMethod(this.ohMethod, proxy);
        this.headers = Elf.checkFixedHeaders(
                this.ohClass, this.ohMethod, this.factory, proxy);
        this.pathVars = Elf.checkFixedPathVars(
                this.ohClass, this.ohMethod, this.factory, proxy);
        this.parameters = Elf.checkFixedParameters(
                this.ohClass, this.ohMethod, this.factory, proxy);
        this.contexts = Elf.checkFixedContexts(
                this.ohClass, this.ohMethod, this.factory, proxy);

        this.statusFallbackMapping = Elf.checkStatusFallbackMapping(this.ohMethod, proxy);
        this.statusSeriesFallbackMapping = Elf.checkStatusSeriesFallbackMapping(this.ohMethod, proxy);

        this.responseParser = Elf.checkResponseParser(this.ohMethod, this.factory, proxy);

        this.extraUrlQueryBuilder = Elf.checkExtraUrlQueryBuilder(this.ohMethod, this.factory, proxy);

        processReturnType(this.ohMethod);
    }

    Object execute(Object[] args) {
        if (this.returnFuture) {
            return ohExecutorService.submit(
                    () -> internalExecute(args));
        }
        return internalExecute(args);
    }

    @SuppressWarnings("unchecked")
    private void processReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        this.returnFuture = Future.class == returnType;
        this.returnCollection = Collection.class.isAssignableFrom(returnType);
        this.returnMap = Map.class.isAssignableFrom(returnType);
        this.returnPair = Pair.class.isAssignableFrom(returnType);
        this.returnTriple = Triple.class.isAssignableFrom(returnType);

        val genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            // 错误的泛型时
            if (this.returnFuture || this.returnCollection ||
                    this.returnPair || this.returnTriple) {
                // 如返回支持的泛型类型则抛出异常
                // 不包括Map<K, V>
                throw new OhException(RETURN_GENERIC_ERROR);
            } else if (genericReturnType instanceof TypeVariable) {
                // 返回类型变量指定的类型时
                // 检查是否为<T extend CncResponse>类型
                checkTypeVariableBounds(genericReturnType);
                this.returnTypes = newArrayList(CncResponse.class);
                return;
            } else {
                // 否则以方法返回类型作为实际返回类型
                // 返回Map时, 可直接解析返回值为Map
                this.returnTypes = newArrayList(returnType);
                return;
            }
        }

        // 方法返回泛型时
        ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (this.returnFuture) {
            // 返回Future类型, 则多处理一层泛型
            val futureTypeArgument = actualTypeArguments[0];
            if (!(futureTypeArgument instanceof ParameterizedType)) {
                if (futureTypeArgument instanceof TypeVariable) {
                    checkTypeVariableBounds(futureTypeArgument);
                    this.returnTypes = newArrayList(CncResponse.class);
                    return;
                }
                this.returnTypes = newArrayList((Class) futureTypeArgument);
                return;
            }

            parameterizedType = (ParameterizedType) futureTypeArgument;
            returnType = (Class) parameterizedType.getRawType();
            this.returnCollection = Collection.class.isAssignableFrom(returnType);
            this.returnMap = Map.class.isAssignableFrom(returnType);
            this.returnPair = Pair.class.isAssignableFrom(returnType);
            this.returnTriple = Triple.class.isAssignableFrom(returnType);
            actualTypeArguments = parameterizedType.getActualTypeArguments();
        }
        if (this.returnCollection || this.returnPair || this.returnTriple) {
            // 以泛型参数类型作为返回值解析目标类型
            this.returnTypes = processActualTypeArguments(actualTypeArguments);
        } else {
            // 以泛型类型作为返回值解析目标类型
            this.returnTypes = newArrayList(returnType);
        }
    }

    private List<Class> processActualTypeArguments(Type[] actualTypeArguments) {
        List<Class> result = newArrayList();
        for (Type actualTypeArgument : actualTypeArguments) {
            if (actualTypeArgument instanceof TypeVariable) {
                checkTypeVariableBounds(actualTypeArgument);
                result.add(CncResponse.class);
                continue;
            }
            result.add((Class) actualTypeArgument);
        }
        return result;
    }

    private void checkTypeVariableBounds(Type type) {
        val bounds = ((TypeVariable) type).getBounds();
        if (bounds.length != 1 || !CncResponse.class
                .isAssignableFrom((Class) bounds[0])) {
            throw new OhException(RETURN_GENERIC_ERROR);
        }
    }

    @SneakyThrows
    private Object internalExecute(Object[] args) {
        val ohCall = new OhCall(this, args);
        val response = ohCall.execute();

        val statusCode = response.code();
        val responseBody = notNullThen(response.body(), OhResponseBody::new);
        if (nonNull(response.body())) response.close();

        val statusFallback = this.statusFallbackMapping
                .get(HttpStatus.valueOf(statusCode));
        if (nonNull(statusFallback)) {
            return applyFallback(statusFallback, statusCode, responseBody);
        }

        val statusSeriesFallback = this.statusSeriesFallbackMapping
                .get(HttpStatus.Series.valueOf(statusCode));
        if (nonNull(statusSeriesFallback)) {
            return applyFallback(statusSeriesFallback, statusCode, responseBody);
        }

        val responseObjs = processResponseBody(
                statusCode, responseBody, ohCall.responseClass);
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
            return Pair.of(responseObjs.get(0),
                    responseObjs.get(1));

        } else if (this.returnTriple) {
            return Triple.of(responseObjs.get(0),
                    responseObjs.get(1), responseObjs.get(2));

        } else {
            return responseObjs.get(0);
        }
    }

    private Object applyFallback(Class<? extends FallbackFunction> function,
                                 int statusCode, ResponseBody responseBody) {
        return FactoryContext.apply(factory, function,
                f -> f.apply(new Response<ResponseBody>(statusCode, responseBody) {
                    @Override
                    public String responseBodyAsString() {
                        return toStr(notNullThen(getResponseBody(),
                                ResponseBodyExtractor::string));
                    }
                }));
    }

    private List<Object> processResponseBody(int statusCode,
                                             ResponseBody responseBody,
                                             Class responseClass) {
        List<Object> returnValues = newArrayList();
        for (val returnType : this.returnTypes) {
            returnValues.add(processReturnTypeValue(statusCode, responseBody,
                    CncResponse.class == returnType ? responseClass : returnType));
        }
        return returnValues;
    }

    private Object processReturnTypeValue(int statusCode,
                                          ResponseBody responseBody,
                                          Class returnType) {
        if (returnVoid(returnType)) {
            return null;
        } else if (returnInteger(returnType)) {
            return statusCode;
        } else if (HttpStatus.class == returnType) {
            return HttpStatus.valueOf(statusCode);
        } else if (HttpStatus.Series.class == returnType) {
            return HttpStatus.Series.valueOf(statusCode);
        } else if (returnBoolean(returnType)) {
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
        } else if (returnUnCollectionString(returnType)) {
            return notNullThen(responseBody, ResponseBodyExtractor::string);
        } else {
            return notNullThen(responseBody, body ->
                    ResponseBodyExtractor.object(body, notNullThen(this.responseParser, parser -> {
                        Map<String, Object> contextMap = this.contexts.stream().collect(
                                HashMap::new, (m, p) -> m.put(p.getKey(), p.getValue()), HashMap::putAll);
                        return content -> parser.parse(content, returnType, contextMap);
                    }), returnType));
        }
    }

    private boolean returnVoid(Class returnType) {
        return void.class == returnType || Void.class == returnType;
    }

    private boolean returnInteger(Class returnType) {
        return int.class == returnType || Integer.class == returnType;
    }

    private boolean returnBoolean(Class returnType) {
        return boolean.class == returnType || Boolean.class == returnType;
    }

    private boolean returnUnCollectionString(Class returnType) {
        return String.class == returnType && !this.returnCollection;
    }

    static class Elf {

        private Elf() {
            throw new UnsupportedOperationException();
        }

        static String checkRequestUrl(Class clazz, Method method,
                                      Factory factory, OhProxy proxy) {
            val mapping = findAnnotation(method, Mapping.class);
            val url = checkNull(mapping, () -> "/" + method.getName(), annotation -> {
                Class<? extends UrlProvider> providerClass = annotation.urlProvider();
                return substitute(UrlProvider.class == providerClass ? annotation.value()
                        : FactoryContext.apply(factory, providerClass, p -> p.url(clazz, method)));
            });
            if (isBlank(url)) return proxy.baseUrl;
            if (isBlank(proxy.baseUrl)) return url;
            return proxy.baseUrl + url;
        }

        static Proxy checkClientProxy(Class clazz, Method method,
                                      Factory factory, OhProxy proxy) {
            val clientProxy = findAnnotation(method, ClientProxy.class);
            return checkNull(clientProxy, () -> proxy.clientProxy, annotation -> {
                val providerClass = annotation.proxyProvider();
                if (ProxyProvider.class == providerClass) {
                    return checkBlank(annotation.host(), () -> null,
                            xx -> new Proxy(annotation.type(), new InetSocketAddress(
                                    annotation.host(), annotation.port())));
                }
                return FactoryContext.apply(factory, providerClass, p -> p.proxy(clazz, method));
            });
        }

        static ClientSSL checkClientSSL(Method method) {
            return findAnnotation(method, ClientSSL.class);
        }

        static SSLSocketFactory checkSSLSocketFactory(Class clazz, Method method,
                                                      Factory factory, ClientSSL clientSSL) {
            val providerClass = clientSSL.sslSocketFactoryProvider();
            if (SSLSocketFactoryProvider.class == providerClass) {
                val factoryClass = clientSSL.sslSocketFactory();
                return SSLSocketFactory.class == factoryClass ? null
                        : FactoryContext.build(factory, factoryClass);
            }
            return FactoryContext.apply(factory, providerClass,
                    p -> p.sslSocketFactory(clazz, method));
        }

        static X509TrustManager checkX509TrustManager(Class clazz, Method method,
                                                      Factory factory, ClientSSL clientSSL) {
            val providerClass = clientSSL.x509TrustManagerProvider();
            if (X509TrustManagerProvider.class == providerClass) {
                val managerClass = clientSSL.x509TrustManager();
                return X509TrustManager.class == managerClass ? null
                        : FactoryContext.build(factory, managerClass);
            }
            return FactoryContext.apply(factory, providerClass,
                    p -> p.x509TrustManager(clazz, method));
        }

        static HostnameVerifier checkHostnameVerifier(Class clazz, Method method,
                                                      Factory factory, ClientSSL clientSSL) {
            val providerClass = clientSSL.hostnameVerifierProvider();
            if (HostnameVerifierProvider.class == providerClass) {
                val verifierClass = clientSSL.hostnameVerifier();
                return HostnameVerifier.class == verifierClass ? null
                        : FactoryContext.build(factory, verifierClass);
            }
            return FactoryContext.apply(factory, providerClass,
                    p -> p.hostnameVerifier(clazz, method));
        }

        static ConnectionPool checkConnectionPool(Method method, OhProxy proxy) {
            val isolated = findAnnotation(method, IsolatedConnectionPool.class);
            return checkNull(isolated, () -> proxy.connectionPool, x -> new ConnectionPool());
        }

        static ClientTimeout checkClientTimeout(Method method) {
            return findAnnotation(method, ClientTimeout.class);
        }

        static long checkCallTimeout(
                Class clazz, Method method, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.callTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.callTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz, method));
        }

        static long checkConnectTimeout(
                Class clazz, Method method, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.connectTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.connectTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz, method));
        }

        static long checkReadTimeout(
                Class clazz, Method method, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.readTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.readTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz, method));
        }

        static long checkWriteTimeout(
                Class clazz, Method method, Factory factory, ClientTimeout clientTimeout) {
            val providerClass = clientTimeout.writeTimeoutProvider();
            return TimeoutProvider.class == providerClass ? clientTimeout.writeTimeout()
                    : FactoryContext.apply(factory, providerClass, p -> p.timeout(clazz, method));
        }

        static List<Interceptor> checkClientInterceptors(Class clazz, Method method, Factory factory, OhProxy proxy) {
            val cleanup = nonNull(findAnnotation(method, ClientInterceptorCleanup.class));
            val result = newArrayList(cleanup ? null : proxy.interceptors);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, ClientInterceptor.class))
                    .stream().filter(annotation -> Interceptor.class != annotation.value()
                            || InterceptorProvider.class != annotation.provider())
                    .map(annotation -> {
                        val providerClass = annotation.provider();
                        if (InterceptorProvider.class == providerClass) {
                            return FactoryContext.build(factory, annotation.value());
                        }
                        return FactoryContext.apply(factory, providerClass, p -> p.interceptor(clazz, method));
                    }).collect(Collectors.toList()));
            return result;
        }

        static Level checkClientLoggingLevel(Class clazz, Method method, Factory factory, OhProxy proxy) {
            val clientLoggingLevel = findAnnotation(method, ClientLoggingLevel.class);
            if (isNull(clientLoggingLevel)) return proxy.loggingLevel;
            val providerClass = clientLoggingLevel.provider();
            return LoggingLevelProvider.class == providerClass ? clientLoggingLevel.value()
                    : FactoryContext.apply(factory, providerClass, p -> p.level(clazz, method));
        }

        @SuppressWarnings("ConstantConditions")
        static OkHttpClient buildOkHttpClient(OhMappingProxy mappingProxy, OhProxy proxy) {
            val sameClientProxy = mappingProxy.clientProxy == proxy.clientProxy;
            val sameSSLSocketFactory = mappingProxy.sslSocketFactory == proxy.sslSocketFactory;
            val sameX509TrustManager = mappingProxy.x509TrustManager == proxy.x509TrustManager;
            val sameHostnameVerifier = mappingProxy.hostnameVerifier == proxy.hostnameVerifier;
            val sameConnectionPool = mappingProxy.connectionPool == proxy.connectionPool;
            val sameCallTimeout = mappingProxy.callTimeout == proxy.callTimeout;
            val sameConnectTimeout = mappingProxy.connectTimeout == proxy.connectTimeout;
            val sameReadTimeout = mappingProxy.readTimeout == proxy.readTimeout;
            val sameWriteTimeout = mappingProxy.writeTimeout == proxy.writeTimeout;
            val sameInterceptors = mappingProxy.interceptors.equals(proxy.interceptors);
            val sameLoggingLevel = mappingProxy.loggingLevel == proxy.loggingLevel;
            if (sameClientProxy && sameSSLSocketFactory && sameX509TrustManager
                    && sameHostnameVerifier && sameConnectionPool
                    && sameCallTimeout && sameConnectTimeout
                    && sameReadTimeout && sameWriteTimeout
                    && sameInterceptors && sameLoggingLevel) return proxy.okHttpClient;

            return new OhReq().clientProxy(mappingProxy.clientProxy)
                    .sslSocketFactory(mappingProxy.sslSocketFactory)
                    .x509TrustManager(mappingProxy.x509TrustManager)
                    .hostnameVerifier(mappingProxy.hostnameVerifier)
                    .connectionPool(mappingProxy.connectionPool)
                    .callTimeout(mappingProxy.callTimeout)
                    .connectTimeout(mappingProxy.connectTimeout)
                    .readTimeout(mappingProxy.readTimeout)
                    .writeTimeout(mappingProxy.writeTimeout)
                    .addInterceptors(mappingProxy.interceptors)
                    .loggingLevel(mappingProxy.loggingLevel)
                    .buildHttpClient();
        }

        static Charset checkAcceptCharset(Method method, OhProxy proxy) {
            val acceptCharset = findAnnotation(method, AcceptCharset.class);
            return checkNull(acceptCharset, () -> proxy.acceptCharset,
                    annotation -> Charset.forName(annotation.value()));
        }

        static ContentFormatter checkContentFormatter(
                Method method, Factory factory, OhProxy proxy) {
            val contentFormat = findAnnotation(method, ContentFormat.class);
            return checkNull(contentFormat, () -> proxy.contentFormatter,
                    annotation -> FactoryContext.build(factory, annotation.value()));
        }

        static HttpMethod checkHttpMethod(Method method, OhProxy proxy) {
            val requestMethod = findAnnotation(method, RequestMethod.class);
            return checkNull(requestMethod, () -> proxy.httpMethod, RequestMethod::value);
        }

        static List<Pair<String, String>> checkFixedHeaders(Class clazz, Method method,
                                                            Factory factory, OhProxy proxy) {
            val result = newArrayList(proxy.headers);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, FixedHeader.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, method, name)));
                    }).collect(Collectors.toList()));
            return result;
        }

        static List<Pair<String, String>> checkFixedPathVars(Class clazz, Method method,
                                                             Factory factory, OhProxy proxy) {
            val result = newArrayList(proxy.pathVars);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, FixedPathVar.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, method, name)));
                    }).collect(Collectors.toList()));
            return result;
        }

        static List<Pair<String, Object>> checkFixedParameters(Class clazz, Method method,
                                                               Factory factory, OhProxy proxy) {
            val result = newArrayList(proxy.parameters);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, FixedParameter.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, method, name))));
                    }).collect(Collectors.toList()));
            return result;
        }

        static List<Pair<String, Object>> checkFixedContexts(Class clazz, Method method,
                                                             Factory factory, OhProxy proxy) {
            val result = newArrayList(proxy.contexts);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, FixedContext.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass
                                ? an.value() : FactoryContext.apply(factory,
                                providerClass, p -> p.value(clazz, method, name))));
                    }).collect(Collectors.toList()));
            return result;
        }

        static Map<HttpStatus, Class<? extends FallbackFunction>>
        checkStatusFallbackMapping(Method method, OhProxy proxy) {
            val result = newHashMap(proxy.statusFallbackMapping);
            result.putAll(newArrayList(findMergedRepeatableAnnotations(
                    method, StatusFallback.class)).stream()
                    .collect(Collectors.toMap(StatusFallback::status,
                            StatusFallback::fallback)));
            return result;
        }

        static Map<HttpStatus.Series, Class<? extends FallbackFunction>>
        checkStatusSeriesFallbackMapping(Method method, OhProxy proxy) {
            val result = newHashMap(proxy.statusSeriesFallbackMapping);
            result.putAll(newArrayList(findMergedRepeatableAnnotations(
                    method, StatusSeriesFallback.class)).stream()
                    .collect(Collectors.toMap(StatusSeriesFallback::statusSeries,
                            StatusSeriesFallback::fallback)));
            return result;
        }

        static ResponseParser checkResponseParser(
                Method method, Factory factory, OhProxy proxy) {
            val responseParse = findAnnotation(method, ResponseParse.class);
            return checkNull(responseParse, () -> proxy.responseParser, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }

        static ExtraUrlQueryBuilder checkExtraUrlQueryBuilder(
                Method method, Factory factory, OhProxy proxy) {
            val extraUrlQuery = findAnnotation(method, ExtraUrlQuery.class);
            return checkNull(extraUrlQuery, () -> proxy.extraUrlQueryBuilder, annotation ->
                    FactoryContext.build(factory, annotation.value()));
        }
    }
}
