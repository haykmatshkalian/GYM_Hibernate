package com.login.gymcrm.dao;

import com.login.gymcrm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InMemoryTrainingDao implements TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Training entity) {
        entityManager.persist(entity);
    }

    @Override
    public void update(Training entity) {
        entityManager.merge(entity);
    }

    @Override
    public void deleteById(String id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public Optional<Training> findById(String id) {
        List<Training> result = entityManager.createQuery(
                        """
                        select tr from Training tr
                        join fetch tr.trainee t
                        join fetch t.user
                        join fetch tr.trainer r
                        join fetch r.user
                        join fetch tr.trainingType
                        where tr.id = :id
                        """, Training.class)
                .setParameter("id", UUID.fromString(id))
                .getResultList();

        return result.stream().findFirst();
    }

    @Override
    public List<Training> findByTraineeUsername(String traineeUsername) {
        return entityManager.createQuery(
                        """
                        select tr from Training tr
                        join fetch tr.trainee t
                        join fetch t.user tu
                        join fetch tr.trainer r
                        join fetch r.user
                        join fetch tr.trainingType
                        where lower(tu.username) = lower(:username)
                        """, Training.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    @Override
    public List<Training> findByTrainerUsername(String trainerUsername) {
        return entityManager.createQuery(
                        """
                        select tr from Training tr
                        join fetch tr.trainee t
                        join fetch t.user
                        join fetch tr.trainer r
                        join fetch r.user ru
                        join fetch tr.trainingType
                        where lower(ru.username) = lower(:username)
                        """, Training.class)
                .setParameter("username", trainerUsername)
                .getResultList();
    }

    @Override
    public List<Training> findAll() {
        return entityManager.createQuery(
                        """
                        select tr from Training tr
                        join fetch tr.trainee t
                        join fetch t.user
                        join fetch tr.trainer r
                        join fetch r.user
                        join fetch tr.trainingType
                        """, Training.class)
                .getResultList();
    }
}
