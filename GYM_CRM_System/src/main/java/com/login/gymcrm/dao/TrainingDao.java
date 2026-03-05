package com.login.gymcrm.dao;

import com.login.gymcrm.model.Training;

import java.util.List;

public interface TrainingDao extends CrudDao<Training> {

    List<Training> findByTraineeUsername(String traineeUsername);

    List<Training> findByTrainerUsername(String trainerUsername);
}
