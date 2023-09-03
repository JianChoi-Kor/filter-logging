package com.example.test.r5;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestLoggingConfig {

    public static int MAX_PAYLOAD_LENGTH = 1024;

    @Bean
    public RequestLoggingFilter loggingFilter() {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeHeaders(true);
        filter.setIncludePayload(true);
        filter.setIncludeQueryString(true);
        filter.setMaxPayloadLength(MAX_PAYLOAD_LENGTH);
        return filter;
    }
}
