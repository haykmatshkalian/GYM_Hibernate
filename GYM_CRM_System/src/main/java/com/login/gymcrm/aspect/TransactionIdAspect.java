package com.login.gymcrm.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class TransactionIdAspect {

    private static final String TRANSACTION_ID = "transactionId";

    @Around("execution(public * com.login.gymcrm.service..*(..)) || execution(public * com.login.gymcrm.facade..*(..))")
    public Object addTransactionId(ProceedingJoinPoint joinPoint) throws Throwable {
        String existing = MDC.get(TRANSACTION_ID);
        boolean created = false;

        if (existing == null) {
            MDC.put(TRANSACTION_ID, UUID.randomUUID().toString());
            created = true;
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (created) {
                MDC.remove(TRANSACTION_ID);
            }
        }
    }
}
