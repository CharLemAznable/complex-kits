package com.github.charlemaznable.core.spring;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static java.util.Objects.isNull;

public final class MutableHttpServletResponse extends HttpServletResponseWrapper {

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private MutableServletOutputStream mutableServletOutputStream;
    private PrintWriter printWriter;

    public MutableHttpServletResponse(HttpServletResponse response) {
        super(response);
        this.mutableServletOutputStream = new MutableServletOutputStream(this.byteArrayOutputStream);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return this.mutableServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() {
        if (isNull(this.printWriter)) {
            this.printWriter = new PrintWriter(this.byteArrayOutputStream);
        }
        return this.printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        this.byteArrayOutputStream.flush();
    }

    @SneakyThrows
    public byte[] getContent() {
        flushBuffer();
        return this.byteArrayOutputStream.toByteArray();
    }

    @SneakyThrows
    public void setContent(byte[] content) {
        this.byteArrayOutputStream.reset();
        this.byteArrayOutputStream.write(content);
    }

    @SneakyThrows
    public void appendContent(byte[] content) {
        this.byteArrayOutputStream.write(content);
    }

    public String getContentAsString() {
        return string(this.getContent());
    }

    public void setContentByString(String content) {
        setContent(bytes(content));
    }

    public void appendContentByString(String content) {
        appendContent(bytes(content));
    }

    public String getContentAsString(Charset charset) {
        return string(this.getContent(), charset);
    }

    public void setContentByString(String content, Charset charset) {
        setContent(bytes(content, charset));
    }

    public void appendContentByString(String content, Charset charset) {
        appendContent(bytes(content, charset));
    }

    @AllArgsConstructor
    static final class MutableServletOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream byteArrayOutputStream;

        @Override
        public void write(int b) {
            this.byteArrayOutputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // ignore WriteListener
        }
    }
}
