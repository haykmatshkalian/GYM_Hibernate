package com.login.gymcrm.dao;

import com.login.gymcrm.model.Trainer;

import java.util.Optional;

public interface TrainerDao extends CrudDao<Trainer> {

    Optional<Trainer> findByUsername(String username);

    Optional<Trainer> findByUserId(String userId);
}
