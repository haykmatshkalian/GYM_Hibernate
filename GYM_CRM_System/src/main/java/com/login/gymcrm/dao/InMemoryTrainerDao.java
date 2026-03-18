package com.login.gymcrm.dao;

import com.login.gymcrm.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InMemoryTrainerDao implements TrainerDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Trainer entity) {
        entityManager.persist(entity);
    }

    @Override
    public void update(Trainer entity) {
        entityManager.merge(entity);
    }

    @Override
    public void deleteById(String id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public Optional<Trainer> findById(String id) {
        List<Trainer> result = entityManager.createQuery(
                        """
                        select distinct t from Trainer t
                        join fetch t.user u
                        left join fetch t.trainees trn
                        left join fetch trn.user
                        where t.id = :id
                        """, Trainer.class)
                .setParameter("id", UUID.fromString(id))
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        List<Trainer> result = entityManager.createQuery(
                        """
                        select distinct t from Trainer t
                        join fetch t.user u
                        left join fetch t.trainees trn
                        left join fetch trn.user
                        where lower(u.username) = lower(:username)
                        """, Trainer.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Optional<Trainer> findByUserId(String userId) {
        List<Trainer> result = entityManager.createQuery(
                        """
                        select distinct t from Trainer t
                        join fetch t.user u
                        left join fetch t.trainees trn
                        left join fetch trn.user
                        where u.id = :userId
                        """, Trainer.class)
                .setParameter("userId", UUID.fromString(userId))
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Trainer> findAll() {
        return entityManager.createQuery(
                        """
                        select distinct t from Trainer t
                        join fetch t.user
                        left join fetch t.trainees trn
                        left join fetch trn.user
                        """, Trainer.class)
                .getResultList();
    }
}
