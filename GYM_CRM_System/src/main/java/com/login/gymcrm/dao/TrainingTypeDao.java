package com.login.gymcrm.dao;

import com.login.gymcrm.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {
    void save(TrainingType entity);

    Optional<TrainingType> findByName(String name);

    Optional<TrainingType> findById(String id);

    List<TrainingType> findAll();
}
