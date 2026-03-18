package com.login.gymcrm.dao;

import com.login.gymcrm.model.User;

import java.util.Optional;

public interface UserDao {

    Optional<User> findByUsername(String username);

    Optional<User> findById(String id);

    void update(User user);
}
