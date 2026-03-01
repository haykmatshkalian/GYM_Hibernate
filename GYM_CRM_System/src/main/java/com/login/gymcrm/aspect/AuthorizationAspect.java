package com.login.gymcrm.aspect;

import com.login.gymcrm.security.Authorized;
import com.login.gymcrm.security.Role;
import com.login.gymcrm.security.SecurityRoleContext;
import com.login.gymcrm.security.exception.AuthorizationException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class AuthorizationAspect {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationAspect.class);

    private final SecurityRoleContext securityRoleContext;

    public AuthorizationAspect(SecurityRoleContext securityRoleContext) {
        this.securityRoleContext = securityRoleContext;
    }

    @Around("@annotation(authorized)")
    public Object enforceAccess(ProceedingJoinPoint joinPoint, Authorized authorized) throws Throwable {
        Role currentRole = securityRoleContext.getCurrentRole();
        boolean allowed = Arrays.stream(authorized.value()).anyMatch(role -> role == currentRole);

        if (!allowed) {
            log.error("Access denied role={} method={}", currentRole, joinPoint.getSignature().toShortString());
            throw new AuthorizationException("Role " + currentRole + " is not allowed to perform this action");
        }

        log.debug("Access granted role={} method={}", currentRole, joinPoint.getSignature().toShortString());
        return joinPoint.proceed();
    }
}
