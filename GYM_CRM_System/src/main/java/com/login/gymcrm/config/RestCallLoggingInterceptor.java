package com.login.gymcrm.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RestCallLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RestCallLoggingInterceptor.class);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String redactedQuery = redactSensitiveQuery(request.getQueryString());
        log.info("REST request method={} path={} query={}",
                request.getMethod(),
                request.getRequestURI(),
                redactedQuery == null ? "" : redactedQuery);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        int status = response.getStatus();
        String message;

        if (ex != null) {
            message = ex.getMessage();
        } else {
            Object responseMessage = request.getAttribute("responseMessage");
            message = responseMessage == null ? (status >= 400 ? "Request failed" : "OK") : responseMessage.toString();
        }

        if (status >= 400) {
            log.warn("REST response method={} path={} status={} message={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    message);
            return;
        }

        log.info("REST response method={} path={} status={} message={}",
                request.getMethod(),
                request.getRequestURI(),
                status,
                message);
    }

    private String redactSensitiveQuery(String query) {
        if (query == null || query.isBlank()) {
            return query;
        }

        String redacted = query.replaceAll("(?i)(password=)[^&]*", "$1***");
        redacted = redacted.replaceAll("(?i)(oldPassword=)[^&]*", "$1***");
        redacted = redacted.replaceAll("(?i)(newPassword=)[^&]*", "$1***");
        return redacted;
    }
}
