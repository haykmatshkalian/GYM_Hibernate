package com.login.gymcrm.dao;

import com.login.gymcrm.model.Trainee;

import java.util.Optional;

public interface TraineeDao extends CrudDao<Trainee> {

    Optional<Trainee> findByUsername(String username);
}
