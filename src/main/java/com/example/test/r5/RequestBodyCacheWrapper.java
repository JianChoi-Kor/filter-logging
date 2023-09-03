package com.example.test.r5;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RequestBodyCacheWrapper extends HttpServletRequestWrapper {

    private final ByteArrayInputStream byteArrayInputStream;

    public RequestBodyCacheWrapper(HttpServletRequest request) throws IOException {
        super(request);
        InputStream is = super.getInputStream();
        byteArrayInputStream = new ByteArrayInputStream(is.readAllBytes());
    }

    @Override
    public ServletInputStream getInputStream() {
        byteArrayInputStream.reset();
        return new ServletInputStream() {
            private final InputStream is = byteArrayInputStream;

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return is.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return is.read(b);
            }
        };
    }
}
