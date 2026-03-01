package com.login.gymcrm.storage;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage {
    private final Map<String, Map<String, ?>> namespaces = new ConcurrentHashMap<>();

    public void registerNamespace(String namespace, Map<String, ?> storage) {
        namespaces.put(namespace, storage);
    }

    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getNamespace(String namespace) {
        Map<String, ?> map = namespaces.get(namespace);
        if (map == null) {
            return Collections.emptyMap();
        }
        return (Map<String, T>) map;
    }

    public Map<String, Map<String, ?>> getAllNamespaces() {
        return Collections.unmodifiableMap(namespaces);
    }
}
