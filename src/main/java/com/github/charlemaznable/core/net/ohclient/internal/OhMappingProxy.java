package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.common.AcceptCharset;
import com.github.charlemaznable.core.net.common.CncResponse;
import com.github.charlemaznable.core.net.common.ContentFormat;
import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
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
import com.github.charlemaznable.core.net.common.StatusError;
import com.github.charlemaznable.core.net.common.StatusErrorMapping;
import com.github.charlemaznable.core.net.common.StatusSeriesErrorMapping;
import com.github.charlemaznable.core.net.ohclient.OhException;
import com.github.charlemaznable.core.net.ohclient.OhReq;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientProxy;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientProxy.ProxyProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientSSL;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientSSL.HostnameVerifierProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientSSL.SSLSocketFactoryProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientSSL.X509TrustManagerProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.IsolatedConnectionPool;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
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
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.substitute;
import static com.github.charlemaznable.core.spring.SpringContext.getBeanOrReflect;
import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public final class OhMappingProxy extends OhRoot {

    private static final String RETURN_GENERIC_ERROR = "Method return type generic Error";

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
        this.requestUrl = Elf.checkRequestUrl(this.ohClass, this.ohMethod, proxy);

        this.clientProxy = Elf.checkClientProxy(this.ohClass, this.ohMethod, proxy);
        val clientSSL = Elf.checkClientSSL(this.ohMethod);
        if (null != clientSSL) {
            this.sslSocketFactory = Elf.checkSSLSocketFactory(
                    this.ohClass, this.ohMethod, clientSSL);
            this.x509TrustManager = Elf.checkX509TrustManager(
                    this.ohClass, this.ohMethod, clientSSL);
            this.hostnameVerifier = Elf.checkHostnameVerifier(
                    this.ohClass, this.ohMethod, clientSSL);
        } else {
            this.sslSocketFactory = proxy.sslSocketFactory;
            this.x509TrustManager = proxy.x509TrustManager;
            this.hostnameVerifier = proxy.hostnameVerifier;
        }
        this.connectionPool = Elf.checkConnectionPool(this.ohMethod, proxy);
        this.okHttpClient = Elf.buildOkHttpClient(this, proxy);

        this.acceptCharset = Elf.checkAcceptCharset(this.ohMethod, proxy);
        this.contentFormatter = Elf.checkContentFormatter(this.ohMethod, proxy);
        this.httpMethod = Elf.checkHttpMethod(this.ohMethod, proxy);
        this.headers = Elf.checkFixedHeaders(this.ohClass, this.ohMethod, proxy);
        this.pathVars = Elf.checkFixedPathVars(this.ohClass, this.ohMethod, proxy);
        this.parameters = Elf.checkFixedParameters(this.ohClass, this.ohMethod, proxy);
        this.contexts = Elf.checkFixedContexts(this.ohClass, this.ohMethod, proxy);

        this.statusErrorMapping = Elf.checkStatusErrorMapping(this.ohMethod, proxy);
        this.statusSeriesErrorMapping = Elf.checkStatusSeriesErrorMapping(this.ohMethod, proxy);

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
        var returnType = method.getReturnType();
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
        var actualTypeArguments = parameterizedType.getActualTypeArguments();
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

        val errorMapping = new StatusErrorFunction(statusCode, responseBody);
        notNullThen(this.statusErrorMapping.get(
                HttpStatus.valueOf(statusCode)), errorMapping);
        notNullThen(this.statusSeriesErrorMapping.get(
                HttpStatus.Series.valueOf(statusCode)), errorMapping);

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
            return Pair.of(responseObjs.get(0), responseObjs.get(1));

        } else if (this.returnTriple) {
            return Triple.of(responseObjs.get(0), responseObjs.get(1), responseObjs.get(2));

        } else {
            return responseObjs.get(0);
        }
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

    private Object processReturnTypeValue(int statusCode, ResponseBody responseBody, Class returnType) {
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
                    ResponseBodyExtractor.object(body, returnType));
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

        static String checkRequestUrl(Class clazz, Method method, OhProxy proxy) {
            val mapping = findAnnotation(method, Mapping.class);
            val url = checkNull(mapping, method::getName, annotation -> {
                val providerClass = annotation.urlProvider();
                return substitute(UrlProvider.class == providerClass ?
                        annotation.value() : getBeanOrReflect(providerClass).url(clazz, method));
            });
            if (isBlank(url)) return proxy.baseUrl;
            if (isBlank(proxy.baseUrl)) return url;
            return removeEnd(proxy.baseUrl, "/") + prependIfMissing(url, "/");
        }

        static Proxy checkClientProxy(Class clazz, Method method, OhProxy proxy) {
            val clientProxy = findAnnotation(method, ClientProxy.class);
            return checkNull(clientProxy, () -> proxy.clientProxy, annotation -> {
                val providerClass = annotation.proxyProvider();
                return ProxyProvider.class == providerClass ?
                        checkBlank(annotation.host(), () -> null,
                                xx -> new Proxy(annotation.type(), new InetSocketAddress(
                                        annotation.host(), annotation.port())))
                        : getBeanOrReflect(providerClass).proxy(clazz, method);
            });
        }

        static ClientSSL checkClientSSL(Method method) {
            return findAnnotation(method, ClientSSL.class);
        }

        static SSLSocketFactory checkSSLSocketFactory(Class clazz, Method method, ClientSSL clientSSL) {
            val providerClass = clientSSL.sslSocketFactoryProvider();
            return SSLSocketFactoryProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).sslSocketFactory(clazz, method);
        }

        static X509TrustManager checkX509TrustManager(Class clazz, Method method, ClientSSL clientSSL) {
            val providerClass = clientSSL.x509TrustManagerProvider();
            return X509TrustManagerProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).x509TrustManager(clazz, method);
        }

        static HostnameVerifier checkHostnameVerifier(Class clazz, Method method, ClientSSL clientSSL) {
            val providerClass = clientSSL.hostnameVerifierProvider();
            return HostnameVerifierProvider.class == providerClass ? null
                    : getBeanOrReflect(providerClass).hostnameVerifier(clazz, method);
        }

        static ConnectionPool checkConnectionPool(Method method, OhProxy proxy) {
            val isolated = findAnnotation(method, IsolatedConnectionPool.class);
            return checkNull(isolated, () -> proxy.connectionPool, x -> new ConnectionPool());
        }

        static OkHttpClient buildOkHttpClient(OhMappingProxy mappingProxy, OhProxy proxy) {
            val sameClientProxy = mappingProxy.clientProxy == proxy.clientProxy;
            val sameSSLSocketFactory = mappingProxy.sslSocketFactory == proxy.sslSocketFactory;
            val sameX509TrustManager = mappingProxy.x509TrustManager == proxy.x509TrustManager;
            val sameHostnameVerifier = mappingProxy.hostnameVerifier == proxy.hostnameVerifier;
            if (sameClientProxy && sameSSLSocketFactory && sameX509TrustManager
                    && sameHostnameVerifier) return proxy.okHttpClient;

            return new OhReq().clientProxy(mappingProxy.clientProxy)
                    .sslSocketFactory(mappingProxy.sslSocketFactory)
                    .x509TrustManager(mappingProxy.x509TrustManager)
                    .hostnameVerifier(mappingProxy.hostnameVerifier)
                    .connectionPool(mappingProxy.connectionPool)
                    .buildHttpClient();
        }

        static Charset checkAcceptCharset(Method method, OhProxy proxy) {
            val acceptCharset = findAnnotation(method, AcceptCharset.class);
            return checkNull(acceptCharset, () -> proxy.acceptCharset,
                    annotation -> Charset.forName(annotation.value()));
        }

        static ContentFormatter checkContentFormatter(Method method, OhProxy proxy) {
            val contentFormat = findAnnotation(method, ContentFormat.class);
            return checkNull(contentFormat, () -> proxy.contentFormatter,
                    annotation -> getBeanOrReflect(annotation.value()));
        }

        static HttpMethod checkHttpMethod(Method method, OhProxy proxy) {
            val requestMethod = findAnnotation(method, RequestMethod.class);
            return checkNull(requestMethod, () -> proxy.httpMethod, RequestMethod::value);
        }

        static List<Pair<String, String>> checkFixedHeaders(Class clazz, Method method, OhProxy proxy) {
            val result = newArrayList(proxy.headers);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, FixedHeader.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, method, name));
                    }).collect(Collectors.toList()));
            return result;
        }

        static List<Pair<String, String>> checkFixedPathVars(Class clazz, Method method, OhProxy proxy) {
            val result = newArrayList(proxy.pathVars);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, FixedPathVar.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, FixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, method, name));
                    }).collect(Collectors.toList()));
            return result;
        }

        static List<Pair<String, Object>> checkFixedParameters(Class clazz, Method method, OhProxy proxy) {
            val result = newArrayList(proxy.parameters);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, FixedParameter.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, method, name)));
                    }).collect(Collectors.toList()));
            return result;
        }

        static List<Pair<String, Object>> checkFixedContexts(Class clazz, Method method, OhProxy proxy) {
            val result = newArrayList(proxy.contexts);
            result.addAll(newArrayList(findMergedRepeatableAnnotations(method, FixedContext.class))
                    .stream().filter(an -> isNotBlank(an.name())).map(an -> {
                        val name = an.name();
                        val providerClass = an.valueProvider();
                        return Pair.of(name, (Object) (FixedValueProvider.class == providerClass ?
                                an.value() : getBeanOrReflect(providerClass).value(clazz, method, name)));
                    }).collect(Collectors.toList()));
            return result;
        }

        static Map<HttpStatus, Class<? extends StatusError>> checkStatusErrorMapping(Method method, OhProxy proxy) {
            val result = newHashMap(proxy.statusErrorMapping);
            result.putAll(newArrayList(findMergedRepeatableAnnotations(method, StatusErrorMapping.class)).stream()
                    .collect(Collectors.toMap(StatusErrorMapping::status, StatusErrorMapping::exception)));
            return result;
        }

        static Map<HttpStatus.Series, Class<? extends StatusError>> checkStatusSeriesErrorMapping(Method method, OhProxy proxy) {
            val result = newHashMap(proxy.statusSeriesErrorMapping);
            result.putAll(newArrayList(findMergedRepeatableAnnotations(method, StatusSeriesErrorMapping.class)).stream()
                    .collect(Collectors.toMap(StatusSeriesErrorMapping::statusSeries, StatusSeriesErrorMapping::exception)));
            return result;
        }
    }
}
