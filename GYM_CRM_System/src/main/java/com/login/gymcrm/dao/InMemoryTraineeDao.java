package com.login.gymcrm.dao;

import com.login.gymcrm.model.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InMemoryTraineeDao implements TraineeDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Trainee entity) {
        entityManager.persist(entity);
    }

    @Override
    public void update(Trainee entity) {
        entityManager.merge(entity);
    }

    @Override
    public void deleteById(String id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public Optional<Trainee> findById(String id) {
        List<Trainee> result = entityManager.createQuery(
                        """
                        select distinct t from Trainee t
                        join fetch t.user u
                        left join fetch t.trainers tr
                        left join fetch tr.user
                        where t.id = :id
                        """, Trainee.class)
                .setParameter("id", UUID.fromString(id))
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        List<Trainee> result = entityManager.createQuery(
                        """
                        select distinct t from Trainee t
                        join fetch t.user u
                        left join fetch t.trainers tr
                        left join fetch tr.user
                        where lower(u.username) = lower(:username)
                        """, Trainee.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Trainee> findAll() {
        return entityManager.createQuery(
                        """
                        select distinct t from Trainee t
                        join fetch t.user
                        left join fetch t.trainers tr
                        left join fetch tr.user
                        """, Trainee.class)
                .getResultList();
    }
}
