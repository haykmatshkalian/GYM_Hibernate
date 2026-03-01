package com.login.gymcrm.dao;

import com.login.gymcrm.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HibernateTrainingTypeDao implements TrainingTypeDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(TrainingType entity) {
        entityManager.persist(entity);
    }

    @Override
    public Optional<TrainingType> findByName(String name) {
        List<TrainingType> result = entityManager.createQuery(
                        "select tt from TrainingType tt where lower(tt.name) = lower(:name)", TrainingType.class)
                .setParameter("name", name)
                .getResultList();
        return result.stream().findFirst();
    }
}
