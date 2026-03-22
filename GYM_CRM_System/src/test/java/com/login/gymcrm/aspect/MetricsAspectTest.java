package com.login.gymcrm.aspect;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetricsAspectTest {

    @Test
    void monitorServiceCallsRecordsCountAndDurationOnSuccess() throws Throwable {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        MetricsAspect metricsAspect = new MetricsAspect(meterRegistry);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(MetricsAspectTest.class);
        when(signature.getName()).thenReturn("demoSuccess");
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = metricsAspect.monitorServiceCalls(joinPoint);

        assertThat(result).isEqualTo("ok");
        assertThat(meterRegistry.get("gymcrm.service.calls.count")
                .tag("class", "MetricsAspectTest")
                .tag("method", "demoSuccess")
                .tag("outcome", "success")
                .counter()
                .count()).isEqualTo(1.0d);

        assertThat(meterRegistry.get("gymcrm.service.calls.duration")
                .tag("class", "MetricsAspectTest")
                .tag("method", "demoSuccess")
                .tag("outcome", "success")
                .timer()
                .count()).isEqualTo(1L);
    }

    @Test
    void monitorApiCallsRecordsErrorOutcomeWhenMethodThrows() throws Throwable {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        MetricsAspect metricsAspect = new MetricsAspect(meterRegistry);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(MetricsAspectTest.class);
        when(signature.getName()).thenReturn("demoError");
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> metricsAspect.monitorApiCalls(joinPoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        assertThat(meterRegistry.get("gymcrm.api.calls.count")
                .tag("class", "MetricsAspectTest")
                .tag("method", "demoError")
                .tag("outcome", "error")
                .counter()
                .count()).isEqualTo(1.0d);
    }
}
