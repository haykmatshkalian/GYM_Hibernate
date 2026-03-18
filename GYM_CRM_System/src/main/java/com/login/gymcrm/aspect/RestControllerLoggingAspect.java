package com.login.gymcrm.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RestControllerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(RestControllerLoggingAspect.class);

    @Around("execution(public * com.login.gymcrm.controller..*(..))")
    public Object logRestCallDetails(ProceedingJoinPoint joinPoint) throws Throwable {
        String endpoint = joinPoint.getSignature().toShortString();
        String requestPayload = sanitize(Arrays.toString(joinPoint.getArgs()));

        log.info("REST endpoint={} request={}", endpoint, requestPayload);

        try {
            Object response = joinPoint.proceed();

            if (response instanceof ResponseEntity<?> responseEntity) {
                Object body = responseEntity.getBody();
                String responseMessage = body == null ? "" : sanitize(body.toString());
                log.info("REST endpoint={} status={} response={}",
                        endpoint,
                        responseEntity.getStatusCode().value(),
                        responseMessage);
            } else {
                log.info("REST endpoint={} status=200 response={}", endpoint, sanitize(String.valueOf(response)));
            }

            return response;
        } catch (Exception ex) {
            log.warn("REST endpoint={} status=error response={}", endpoint, sanitize(ex.getMessage()));
            throw ex;
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }

        String sanitized = value.replaceAll("(?i)(password=)([^,\\]]+)", "$1***");
        sanitized = sanitized.replaceAll("(?i)(oldPassword=)([^,\\]]+)", "$1***");
        sanitized = sanitized.replaceAll("(?i)(newPassword=)([^,\\]]+)", "$1***");
        return sanitized;
    }
}
