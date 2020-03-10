package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.lang.Str;
import com.github.charlemaznable.core.net.common.Bundle;
import com.github.charlemaznable.core.net.common.CncRequest;
import com.github.charlemaznable.core.net.common.CncResponse.CncResponseImpl;
import com.github.charlemaznable.core.net.common.Context;
import com.github.charlemaznable.core.net.common.Header;
import com.github.charlemaznable.core.net.common.Parameter;
import com.github.charlemaznable.core.net.common.PathVar;
import com.github.charlemaznable.core.net.common.RequestBodyRaw;
import com.github.charlemaznable.core.net.ohclient.OhReq;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientTimeout;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.joor.ReflectException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

import static com.github.charlemaznable.core.codec.Json.desc;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.CONTENT_TYPE;
import static com.github.charlemaznable.core.net.ohclient.internal.OhDummy.log;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.joor.Reflect.accessible;
import static org.joor.Reflect.on;

public final class OhCall extends OhRoot {

    Class responseClass = CncResponseImpl.class;
    String requestBodyRaw;
    Request request;

    OhCall(OhMappingProxy proxy, Object[] args) {
        initial(proxy);
        processArguments(proxy.ohMethod, args);
        this.okHttpClient = buildOkHttpClient(proxy);
        this.request = buildRequest(proxy.requestUrl);
    }

    @SneakyThrows
    Response execute() {
        return this.okHttpClient.newCall(this.request).execute();
    }

    private void initial(OhMappingProxy proxy) {
        this.clientProxy = proxy.clientProxy;
        this.sslSocketFactory = proxy.sslSocketFactory;
        this.x509TrustManager = proxy.x509TrustManager;
        this.hostnameVerifier = proxy.hostnameVerifier;
        this.connectionPool = proxy.connectionPool;
        this.callTimeout = proxy.callTimeout;
        this.connectTimeout = proxy.connectTimeout;
        this.readTimeout = proxy.readTimeout;
        this.writeTimeout = proxy.writeTimeout;

        this.acceptCharset = proxy.acceptCharset;
        this.contentFormatter = proxy.contentFormatter;
        this.httpMethod = proxy.httpMethod;
        this.headers = newArrayList(proxy.headers);
        this.pathVars = newArrayList(proxy.pathVars);
        this.parameters = newArrayList(proxy.parameters);
        this.contexts = newArrayList(proxy.contexts);
    }

    private void processArguments(Method method, Object[] args) {
        val parameterAnnotationsArray = method.getParameterAnnotations();
        val parameterTypes = method.getParameterTypes();
        for (int i = 0, count = args.length; i < count; i++) {
            val argument = args[i];
            val parameterAnnotations = parameterAnnotationsArray[i];
            val parameterType = parameterTypes[i];

            val configuredType = processParameterType(argument, parameterType);
            if (configuredType) continue;
            processAnnotations(argument, parameterAnnotations, null);
        }
    }

    private boolean processParameterType(Object argument, Class parameterType) {
        if (Proxy.class.isAssignableFrom(parameterType)) {
            this.clientProxy = (Proxy) argument;
        } else if (SSLSocketFactory.class.isAssignableFrom(parameterType)) {
            this.sslSocketFactory = (SSLSocketFactory) argument;
        } else if (X509TrustManager.class.isAssignableFrom(parameterType)) {
            this.x509TrustManager = (X509TrustManager) argument;
        } else if (HostnameVerifier.class.isAssignableFrom(parameterType)) {
            this.hostnameVerifier = (HostnameVerifier) argument;
        } else if (ClientTimeout.class.isAssignableFrom(parameterType)) {
            if (nonNull(argument)) {
                ClientTimeout clientTimeout = (ClientTimeout) argument;
                this.callTimeout = clientTimeout.callTimeout();
                this.connectTimeout = clientTimeout.connectTimeout();
                this.readTimeout = clientTimeout.readTimeout();
                this.writeTimeout = clientTimeout.writeTimeout();
            }
        } else if (CncRequest.class.isAssignableFrom(parameterType)) {
            this.responseClass = checkNull(argument,
                    () -> CncResponseImpl.class,
                    xx -> ((CncRequest) xx).responseClass());
            return false;
        } else {
            return false;
        }
        return true;
    }

    private void processAnnotations(Object argument, Annotation[] annotations,
                                    String defaultParameterName) {
        boolean processed = false;
        for (Annotation annotation : annotations) {
            if (annotation instanceof Header) {
                processHeader(argument, (Header) annotation);
                processed = true;
            } else if (annotation instanceof PathVar) {
                processPathVar(argument, (PathVar) annotation);
                processed = true;
            } else if (annotation instanceof Parameter) {
                processParameter(argument, (Parameter) annotation);
                processed = true;
            } else if (annotation instanceof Context) {
                processContext(argument, (Context) annotation);
                processed = true;
            } else if (annotation instanceof RequestBodyRaw) {
                processRequestBodyRaw(argument);
                processed = true;
            } else if (annotation instanceof Bundle) {
                processBundle(argument);
                processed = true;
            }
        }
        if (!processed && nonNull(defaultParameterName)) {
            processParameter(argument, new ParameterImpl(defaultParameterName));
        }
    }

    private void processHeader(Object argument, Header header) {
        this.headers.add(Pair.of(header.value(),
                notNullThen(argument, Str::toStr)));
    }

    private void processPathVar(Object argument, PathVar pathVar) {
        this.pathVars.add(Pair.of(pathVar.value(),
                notNullThen(argument, Str::toStr)));
    }

    private void processParameter(Object argument, Parameter parameter) {
        this.parameters.add(Pair.of(parameter.value(), argument));
    }

    private void processContext(Object argument, Context context) {
        this.contexts.add(Pair.of(context.value(), argument));
    }

    private void processRequestBodyRaw(Object argument) {
        if (null == argument || (argument instanceof String)) {
            // OhRequestBodyRaw参数传值null时, 则继续使用parameters构造请求
            this.requestBodyRaw = (String) argument;
            return;
        }
        log.warn("Argument annotated with @RequestBodyRaw, " +
                "but Type is {} instead String.", argument.getClass());
    }

    private void processBundle(Object argument) {
        if (isNull(argument)) return;
        if (argument instanceof Map) {
            Map<Object, Object> argMap = desc(argument);
            for (val entry : argMap.entrySet()) {
                processParameter(entry.getValue(),
                        new ParameterImpl(toStr(entry.getKey())));
            }
            return;
        }

        val clazz = argument.getClass();
        val reflect = on(argument);
        val fields = reflect.fields();
        for (val fieldEntry : fields.entrySet()) {
            val fieldName = fieldEntry.getKey();
            val field = field0(clazz, fieldName);
            val fieldReflect = fieldEntry.getValue();
            val fieldValue = fieldReflect.get();

            Annotation[] annotations = field.getAnnotations();
            processAnnotations(fieldValue, annotations, fieldName);
        }
    }

    private Field field0(Class<?> clazz, String fieldName) {
        Class<?> t = clazz;
        try {
            return accessible(t.getField(fieldName));
        } catch (NoSuchFieldException e) {
            do {
                try {
                    return accessible(t.getDeclaredField(fieldName));
                } catch (NoSuchFieldException ignore) {
                    // ignored
                }
                t = t.getSuperclass();
            } while (t != null);
            throw new ReflectException(e);
        }
    }

    private OkHttpClient buildOkHttpClient(OhMappingProxy proxy) {
        val sameClientProxy = this.clientProxy == proxy.clientProxy;
        val sameSSLSocketFactory = this.sslSocketFactory == proxy.sslSocketFactory;
        val sameX509TrustManager = this.x509TrustManager == proxy.x509TrustManager;
        val sameHostnameVerifier = this.hostnameVerifier == proxy.hostnameVerifier;
        val sameCallTimeout = this.callTimeout == proxy.callTimeout;
        val sameConnectTimeout = this.connectTimeout == proxy.connectTimeout;
        val sameReadTimeout = this.readTimeout == proxy.readTimeout;
        val sameWriteTimeout = this.writeTimeout == proxy.writeTimeout;
        if (sameClientProxy && sameSSLSocketFactory
                && sameX509TrustManager && sameHostnameVerifier
                && sameCallTimeout && sameConnectTimeout &&
                sameReadTimeout && sameWriteTimeout) return proxy.okHttpClient;

        return new OhReq().clientProxy(this.clientProxy)
                .sslSocketFactory(this.sslSocketFactory)
                .x509TrustManager(this.x509TrustManager)
                .hostnameVerifier(this.hostnameVerifier)
                .connectionPool(this.connectionPool)
                .callTimeout(this.callTimeout)
                .connectTimeout(this.connectTimeout)
                .readTimeout(this.readTimeout)
                .writeTimeout(this.writeTimeout)
                .buildHttpClient();
    }

    private Request buildRequest(String url) {
        val requestBuilder = new Request.Builder();

        val acceptCharsetName = this.acceptCharset.name();
        requestBuilder.header(ACCEPT_CHARSET, acceptCharsetName);
        val contentType = this.contentFormatter.contentType();
        requestBuilder.header(CONTENT_TYPE, contentType);
        for (val header : this.headers) {
            checkNull(header.getValue(),
                    () -> requestBuilder.removeHeader(header.getKey()),
                    xx -> requestBuilder.header(header.getKey(), header.getValue()));
        }

        Map<String, String> pathVarMap = this.pathVars.stream().collect(
                HashMap::new, (m, p) -> m.put(p.getKey(), p.getValue()), HashMap::putAll);
        val pathVarSubstitutor = new StringSubstitutor(pathVarMap, "{", "}");
        val requestUrl = pathVarSubstitutor.replace(url);
        Map<String, Object> parameterMap = this.parameters.stream().collect(
                HashMap::new, (m, p) -> m.put(p.getKey(), p.getValue()), HashMap::putAll);
        Map<String, Object> contextMap = this.contexts.stream().collect(
                HashMap::new, (m, p) -> m.put(p.getKey(), p.getValue()), HashMap::putAll);

        val requestMethod = this.httpMethod.toString();
        if (!HttpMethod.permitsRequestBody(requestMethod)) {
            requestBuilder.method(requestMethod, null);
            val addQuery = this.contentFormatter.format(parameterMap, contextMap);
            if (isBlank(addQuery)) requestBuilder.url(requestUrl);
            else requestBuilder.url(requestUrl +
                    (requestUrl.contains("?") ? "&" : "?") + addQuery);
        } else {
            val content = nullThen(this.requestBodyRaw, () ->
                    this.contentFormatter.format(parameterMap, contextMap));
            requestBuilder.method(requestMethod, RequestBody.create(
                    MediaType.parse(contentType), content));
            requestBuilder.url(requestUrl);
        }
        return requestBuilder.build();
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    @AllArgsConstructor
    private static class ParameterImpl implements Parameter {

        private String value;

        @Override
        public String value() {
            return value;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Parameter.class;
        }
    }
}
