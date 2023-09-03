package com.example.test.r5;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {
    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        log.info(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        log.info(message);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;

        if (isIncludePayload() && isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        long start = System.currentTimeMillis();

        if (isFirstRequest) {
            beforeRequest(requestToUse, getBeforeMessage(requestToUse));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!isAsyncStarted(requestToUse)) {
                afterRequest(requestToUse, getAfterMessage(System.currentTimeMillis() - start, response.getStatus()));
            }
        }
    }

    private String getBeforeMessage(HttpServletRequest request) {
        return createMessage(request, "Request: ", "");
    }

    private String getAfterMessage(long elapsed, int status) {
        return "Response: " + elapsed + "ms, " + status;
    }

    protected String createMessage(HttpServletRequest request, String prefix, String suffix) {
        StringBuilder msg = new StringBuilder();
        msg.append(prefix);
        msg.append(request.getMethod()).append(' ');
        msg.append(request.getRequestURI());

        if (isIncludeQueryString()) {
            String queryString = request.getQueryString();
            if (queryString != null) {
                msg.append('?').append(queryString);
            }
        }

        if (isIncludeClientInfo()) {
            String client = request.getRemoteAddr();
            if (StringUtils.hasLength(client)) {
                msg.append(", client=").append(client);
            }
            HttpSession session = request.getSession(false);
            if (session != null) {
                msg.append(", session=").append(session.getId());
            }
            String user = request.getRemoteUser();
            if (user != null) {
                msg.append(", user=").append(user);
            }
        }

        if (isIncludeHeaders()) {
            HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
            if (getHeaderPredicate() != null) {
                Enumeration<String> names = request.getHeaderNames();
                while (names.hasMoreElements()) {
                    String header = names.nextElement();
                    if (!getHeaderPredicate().test(header)) {
                        headers.set(header, "masked");
                    }
                }
            }
            msg.append(", headers=").append(headers);
        }

        if (isIncludePayload()) {
            String payload = null;
            try {
                payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("failed to read payload", e);
            }
            if (payload != null) {
                msg.append(", payload=").append(payload);
            }
        }

        msg.append(suffix);
        return msg.toString();
    }
}
