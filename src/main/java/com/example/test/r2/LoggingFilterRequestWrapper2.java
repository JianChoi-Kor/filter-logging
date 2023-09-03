package com.example.test.r2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
//@Component
public class LoggingFilterRequestWrapper2 extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (isAsyncDispatch(request)) {
      filterChain.doFilter(request, response);
    } else {
      doFilterWrapped(new RequestWrapper2(request), new ContentCachingResponseWrapper(response), filterChain);
    }
  }

  protected void doFilterWrapped(RequestWrapper2 request, ContentCachingResponseWrapper response,
                                 FilterChain filterChain) throws IOException, ServletException {
    try {
      logRequest(request);
      filterChain.doFilter(request, response);
    } finally {
      logResponse(response);
      response.copyBodyToResponse();
    }
  }

  private static void logRequest(RequestWrapper2 request) throws IOException {
    String queryString = request.getQueryString();
    log.info("Request : {} uri=[{}] content-type[{}]", request.getMethod(),
        queryString == null ? request.getRequestURI() : request.getRequestURI() + queryString,
        request.getContentType());

    logPayload("Request", request.getContentType(), request.getInputStream());
  }

  private static void logResponse(ContentCachingResponseWrapper response) throws IOException {
    logPayload("Response", response.getContentType(), response.getContentInputStream());
  }

  private static void logPayload(String prefix, String contentType, InputStream inputStream) throws IOException {
    boolean visible = isVisible(MediaType.valueOf(contentType == null ? "application/json" : contentType));

    if (visible) {
      byte[] content = StreamUtils.copyToByteArray(inputStream);
      if (content.length > 0) {
        String contentString = new String(content);
        log.info("{} Payload: {}", prefix, contentString);
      }
    } else {
      log.info("{} Payload: Binary Content", prefix);
    }
  }

  private static boolean isVisible(MediaType mediaType) {
    final List<MediaType> VISIBLE_TYPES = Arrays.asList(
        MediaType.valueOf("text/*"),
        MediaType.APPLICATION_FORM_URLENCODED,
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML,
        MediaType.valueOf("application/*+json"),
        MediaType.valueOf("application/*+xml"),
        MediaType.MULTIPART_FORM_DATA
    );

    return VISIBLE_TYPES.stream()
        .anyMatch(visibleType -> visibleType.includes(mediaType));
  }
}

