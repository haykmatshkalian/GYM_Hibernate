package com.login.gymcrm.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnBean(MeterRegistry.class)
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    public MetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(public * com.login.gymcrm.controller..*(..))")
    public Object monitorApiCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitor(joinPoint, "gymcrm.api.calls");
    }

    @Around("execution(public * com.login.gymcrm.service..*(..))")
    public Object monitorServiceCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitor(joinPoint, "gymcrm.service.calls");
    }

    private Object monitor(ProceedingJoinPoint joinPoint, String metricPrefix) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            outcome = "error";
            throw throwable;
        } finally {
            Counter.builder(metricPrefix + ".count")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("outcome", outcome)
                    .register(meterRegistry)
                    .increment();

            sample.stop(
                    Timer.builder(metricPrefix + ".duration")
                            .tag("class", className)
                            .tag("method", methodName)
                            .tag("outcome", outcome)
                            .register(meterRegistry)
            );
        }
    }
}
