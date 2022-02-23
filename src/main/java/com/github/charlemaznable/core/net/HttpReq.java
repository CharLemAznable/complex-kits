package com.github.charlemaznable.core.net;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Str.isEmpty;
import static com.github.charlemaznable.core.net.Url.build;
import static com.github.charlemaznable.core.net.Url.encode;
import static java.lang.String.format;
import static java.net.HttpURLConnection.setFollowRedirects;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.tuple.Pair.of;

@Slf4j
public final class HttpReq {

    private final String baseUrl;

    private String req;

    private Charset charset = UTF_8;

    private StringBuilder params = new StringBuilder();

    private List<Pair<String, String>> props = newArrayList();

    private SSLSocketFactory sslSocketFactory;

    private HostnameVerifier hostnameVerifier;

    private Proxy proxy;

    public HttpReq(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public HttpReq(String baseUrlTemplate, Object... baseUrlArgs) {
        this(format(baseUrlTemplate, baseUrlArgs));
    }

    public static String get(String baseUrl) {
        return new HttpReq(baseUrl).get();
    }

    public static String get(String baseUrlTemplate, Object... baseUrlArgs) {
        return get(format(baseUrlTemplate, baseUrlArgs));
    }

    public HttpReq req(String req) {
        this.req = req;
        return this;
    }

    public HttpReq cookie(String value) {
        if (isNull(value)) return this;
        return prop("Cookie", value);
    }

    public HttpReq prop(String name, String value) {
        props.add(of(name, value));
        return this;
    }

    public HttpReq param(String name, String value) {
        if (params.length() > 0) params.append('&');
        params.append(name).append('=').append(encode(value));
        return this;
    }

    public HttpReq params(Map<String, String> params) {
        for (val paramEntry : params.entrySet()) {
            val key = paramEntry.getKey();
            val value = paramEntry.getValue();
            if (isEmpty(key) || isEmpty(value)) continue;
            param(key, value);
        }
        return this;
    }

    public HttpReq requestBody(String requestBody) {
        if (nonNull(requestBody)) {
            if (params.length() > 0) params.append('&');
            params.append(requestBody);
        }
        return this;
    }

    public HttpReq sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public HttpReq hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public HttpReq proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public String post() {
        HttpURLConnection http = null;
        try {
            // Post请求的url，与get不同的是不需要带参数
            val url = baseUrl + (isNull(req) ? "" : req);

            http = commonSettings(url);
            postSettings(http);
            setHeaders(http);
            setSSL(http);
            // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
            // 要注意的是connection.getOutputStream会隐含的进行connect。
            http.connect();
            writePostRequestBody(http);

            return parseResponse(http, url);
        } catch (Exception e) {
            log.error("post error {}", e.getMessage());
            return null;
        } finally {
            if (nonNull(http)) http.disconnect();
        }
    }

    public String get() {
        HttpURLConnection http = null;
        try {
            val url = baseUrl + (isNull(req) ? "" : req)
                    + (params.length() > 0 ? ("?" + params) : "");

            http = commonSettings(url);
            setHeaders(http);
            setSSL(http);
            http.connect();

            return parseResponse(http, url);
        } catch (Exception e) {
            log.error("get error {}", e.getMessage());
            return null;
        } finally {
            if (nonNull(http)) http.disconnect();
        }
    }

    private HttpURLConnection commonSettings(String urlString) throws IOException {
        setFollowRedirects(true);
        val url = build(urlString);
        val http = (HttpURLConnection) (isNull(this.proxy) ?
                url.openConnection() : url.openConnection(this.proxy));
        http.setRequestProperty("Accept-Charset", this.charset.name());
        http.setConnectTimeout(60 * 1000);
        http.setReadTimeout(60 * 1000);
        return http;
    }

    private void postSettings(HttpURLConnection http) throws ProtocolException {
        // 设置是否向connection输出，因为这个是post请求，参数要放在
        // http正文内，因此需要设为true
        http.setDoOutput(true);
        http.setDoInput(true); // Read from the connection. Default is true.
        http.setRequestMethod("POST");// 默认是 GET方式
        http.setUseCaches(false); // Post 请求不能使用缓存
        http.setInstanceFollowRedirects(true);
        // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
        // 意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode进行编码
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    }

    private void setHeaders(HttpURLConnection http) {
        for (val prop : props) http.setRequestProperty(prop.getKey(), prop.getValue());
    }

    private void setSSL(HttpURLConnection http) {
        if (!(http instanceof HttpsURLConnection)) return;

        if (nonNull(sslSocketFactory))
            ((HttpsURLConnection) http).setSSLSocketFactory(sslSocketFactory);

        if (nonNull(hostnameVerifier))
            ((HttpsURLConnection) http).setHostnameVerifier(hostnameVerifier);
    }

    private void writePostRequestBody(HttpURLConnection http) throws IOException {
        if (params.length() == 0) return;

        val out = new DataOutputStream(http.getOutputStream());
        // The URL-encoded contend 正文，正文内容其实跟get的URL中 '? '后的参数字符串一致
        // DataOutputStream.writeBytes将字符串中的16位的unicode字符以8位的字符形式写到流里面
        // 错误用法: out.writeBytes(postData)
        val postData = params.toString();
        out.write(postData.getBytes(this.charset));
        out.flush();
        out.close();
    }

    private String parseResponse(HttpURLConnection http, String url) throws IOException {
        val status = http.getResponseCode();
        val rspCharset = parseCharset(http.getHeaderField("Content-Type"));

        if (status == 200) return readResponseBody(http, rspCharset);

        log.warn("non 200 response :" + readErrorResponseBody(url, http, status, rspCharset));
        return null;
    }

    private Charset parseCharset(String contentType) {
        if (isNull(contentType)) return this.charset;

        String charsetName = null;
        for (val param : contentType.replace(" ", "").split(";")) {
            if (param.startsWith("charset=")) {
                charsetName = param.split("=", 2)[1];
                break;
            }
        }

        return isNull(charsetName) ? this.charset : Charset.forName(charsetName);
    }

    private String readResponseBody(HttpURLConnection http, Charset charset) throws IOException {
        return readInputStreamToString(http.getInputStream(), charset);
    }

    private String readErrorResponseBody(String url, HttpURLConnection http, int status, Charset charset) throws IOException {
        val errorStream = http.getErrorStream();
        if (nonNull(errorStream)) {
            val error = readInputStreamToString(errorStream, charset);
            return (url + ", STATUS CODE =" + status + ", headers=" + json(http.getHeaderFields()) + "\n\n" + error);
        } else {
            return (url + ", STATUS CODE =" + status + ", headers=" + json(http.getHeaderFields()));
        }
    }

    private String readInputStreamToString(InputStream inputStream, Charset charset) throws IOException {
        val baos = new ByteArrayOutputStream();
        val buffer = new byte[1024];

        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        return new String(baos.toByteArray(), charset);
    }
}
