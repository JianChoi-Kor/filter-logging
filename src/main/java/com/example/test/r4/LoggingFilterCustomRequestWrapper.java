package com.example.test.r4;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
//@Component
public class LoggingFilterCustomRequestWrapper extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (isAsyncDispatch(request)) {
      filterChain.doFilter(request, response);
    } else {
      doFilterWrapped(new CustomRequestWrapper(request), new ContentCachingResponseWrapper(response), filterChain);
    }
  }

  protected void doFilterWrapped(CustomRequestWrapper request, ContentCachingResponseWrapper response,
                                 FilterChain filterChain) throws IOException, ServletException {
    try {
      filterChain.doFilter(request, response);
      logRequest(request);
    } finally {
      logResponse(response);
      response.copyBodyToResponse();
    }
  }

  private static void logRequest(CustomRequestWrapper request) throws IOException {
    String queryString = request.getQueryString();
    log.info("Request : {} uri=[{}] content-type[{}]", request.getMethod(),
        queryString == null ? request.getRequestURI() : request.getRequestURI() + "?" + queryString,
        request.getContentType());

    logPayload("Request", request.getContentType(), request.getContentAsByteArray());
  }

  private static void logResponse(ContentCachingResponseWrapper response) throws IOException {
    logPayload("Response", response.getContentType(), response.getContentAsByteArray());
  }

  private static void logPayload(String prefix, String contentType, byte[] rowData) throws IOException {
    boolean visible = isVisible(MediaType.valueOf(contentType == null ? "application/json" : contentType));

    if (visible) {
      if (rowData.length > 0) {
        String contentString = new String(rowData);
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


