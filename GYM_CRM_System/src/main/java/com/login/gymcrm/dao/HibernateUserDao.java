package com.login.gymcrm.dao;

import com.login.gymcrm.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class HibernateUserDao implements UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> findByUsername(String username) {
        List<User> result = entityManager.createQuery(
                        "select u from User u where lower(u.username) = lower(:username)", User.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Optional<User> findById(String id) {
        List<User> result = entityManager.createQuery(
                        "select u from User u where u.id = :id", User.class)
                .setParameter("id", UUID.fromString(id))
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public void update(User user) {
        entityManager.merge(user);
    }
}
