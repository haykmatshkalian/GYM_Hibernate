package com.login.gymcrm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityRoleContext {
    private static final ThreadLocal<Role> ROLE_HOLDER = new ThreadLocal<>();

    private final Role defaultRole;

    public SecurityRoleContext(@Value("${security.default.role:ADMIN}") String defaultRole) {
        this.defaultRole = Role.valueOf(defaultRole.trim().toUpperCase());
    }

    public Role getCurrentRole() {
        Role role = ROLE_HOLDER.get();
        return role == null ? defaultRole : role;
    }

    public void setCurrentRole(Role role) {
        ROLE_HOLDER.set(role);
    }

    public void clear() {
        ROLE_HOLDER.remove();
    }
}
