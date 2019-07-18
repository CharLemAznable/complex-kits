package com.github.charlemaznable.net;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.Charsets;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.codec.Json.json;
import static com.github.charlemaznable.lang.Listt.newArrayList;
import static com.github.charlemaznable.lang.Str.isEmpty;
import static com.github.charlemaznable.net.Url.encode;
import static java.lang.String.format;
import static java.net.HttpURLConnection.setFollowRedirects;
import static org.apache.commons.lang3.tuple.Pair.of;

@Slf4j
public class HttpReq {

    private final String baseUrl;

    private String req;

    private Charset charset = Charsets.UTF_8;

    private StringBuilder params = new StringBuilder();

    private List<Pair<String, String>> props = newArrayList();

    private SSLSocketFactory sslSocketFactory;

    private HostnameVerifier hostnameVerifier;

    public HttpReq(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public HttpReq(String baseUrlTemplate, Object... baseUrlArgs) {
        this.baseUrl = format(baseUrlTemplate, baseUrlArgs);
    }

    public static String get(String baseUrl) {
        return new HttpReq(baseUrl).get();
    }

    public static String get(String baseUrlTemplate, Object... baseUrlArgs) {
        return new HttpReq(baseUrlTemplate, baseUrlArgs).get();
    }

    private static String readResponseBody(HttpURLConnection http, Charset charset) throws IOException {
        return toString(http.getInputStream(), charset);
    }

    private static String toString(InputStream inputStream, Charset charset) throws IOException {
        val baos = new ByteArrayOutputStream();
        val buffer = new byte[1024];

        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        return new String(baos.toByteArray(), charset);
    }

    private Charset parseCharset(String contentType) {
        if (contentType == null) return this.charset;

        String charsetName = null;
        for (val param : contentType.replace(" ", "").split(";")) {
            if (param.startsWith("charset=")) {
                charsetName = param.split("=", 2)[1];
                break;
            }
        }

        return charsetName == null ?
                this.charset : Charset.forName(charsetName);
    }

    public HttpReq req(String req) {
        this.req = req;
        return this;
    }

    public HttpReq cookie(String value) {
        if (value == null) return this;

        return prop("Cookie", value);
    }

    public HttpReq prop(String name, String value) {
        props.add(of(name, value));
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public HttpReq param(String name, String value) {
        if (params.length() > 0) params.append('&');
        params.append(name).append('=').append(encode(value));

        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
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
        if (requestBody != null) {
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

    public String post() {
        HttpURLConnection http = null;
        try {
            // Post请求的url，与get不同的是不需要带参数
            val url = baseUrl + (req == null ? "" : req);

            http = commonSettings(url);
            postSettings(http);
            setHeaders(http);

            if (sslSocketFactory != null)
                ((HttpsURLConnection) http).setSSLSocketFactory(sslSocketFactory);

            if (hostnameVerifier != null)
                ((HttpsURLConnection) http).setHostnameVerifier(hostnameVerifier);

            // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
            // 要注意的是connection.getOutputStream会隐含的进行connect。
            http.connect();

            writePostRequestBody(http);

            return parseResponse(http, url);
        } catch (Exception e) {
            log.error("post error {}", e.getMessage());
            return null;
        } finally {
            if (http != null) http.disconnect();
        }
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

    public String get() {
        HttpURLConnection http = null;
        try {
            val url = baseUrl + (req == null ? "" : req)
                    + (params.length() > 0 ? ("?" + params) : "");

            http = commonSettings(url);
            setHeaders(http);

            if (sslSocketFactory != null)
                ((HttpsURLConnection) http).setSSLSocketFactory(sslSocketFactory);

            if (hostnameVerifier != null)
                ((HttpsURLConnection) http).setHostnameVerifier(hostnameVerifier);

            http.connect();

            return parseResponse(http, url);
        } catch (Exception e) {
            log.error("get error {}", e.getMessage());
            return null;
        } finally {
            if (http != null) http.disconnect();
        }
    }

    private void setHeaders(HttpURLConnection http) {
        for (val prop : props) http.setRequestProperty(prop.getKey(), prop.getValue());
    }

    private HttpURLConnection commonSettings(String url) throws IOException {
        setFollowRedirects(true);
        val http = (HttpURLConnection) new URL(url).openConnection();
        http.setRequestProperty("Accept-Charset", this.charset.name());
        http.setConnectTimeout(60 * 1000);
        http.setReadTimeout(60 * 1000);
        return http;
    }

    private void writePostRequestBody(HttpURLConnection http) throws IOException {
        if (params.length() == 0) return;

        val out = new DataOutputStream(http.getOutputStream());
        // The URL-encoded contend 正文，正文内容其实跟get的URL中 '? '后的参数字符串一致
        // DataOutputStream.writeBytes将字符串中的16位的unicode字符以8位的字符形式写到流里面
        // out.writeBytes(postData);
        val postData = params.toString();
        out.write(postData.getBytes(this.charset));
        out.flush();
        out.close();
    }

    private String parseResponse(HttpURLConnection http, String url) throws IOException {
        val status = http.getResponseCode();
        val charset = parseCharset(http.getHeaderField("Content-Type"));

        if (status == 200) return readResponseBody(http, charset);

        log.warn("non 200 response :" + readErrorResponseBody(url, http, status, charset));
        return null;
    }

    private String readErrorResponseBody(String url, HttpURLConnection http, int status, Charset charset) throws IOException {
        val errorStream = http.getErrorStream();
        if (errorStream != null) {
            val error = toString(errorStream, charset);
            return (url + ", STATUS CODE =" + status + ", headers=" + json(http.getHeaderFields()) + "\n\n" + error);
        } else {
            return (url + ", STATUS CODE =" + status + ", headers=" + json(http.getHeaderFields()));
        }
    }
}
