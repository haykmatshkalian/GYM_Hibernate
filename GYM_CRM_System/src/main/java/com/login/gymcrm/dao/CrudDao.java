package com.login.gymcrm.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDao<T> {
    void save(T entity);

    void update(T entity);

    void deleteById(String id);

    Optional<T> findById(String id);

    List<T> findAll();
}
