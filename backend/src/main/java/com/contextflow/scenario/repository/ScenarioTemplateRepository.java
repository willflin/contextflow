package com.contextflow.scenario.repository;

import com.contextflow.scenario.domain.ScenarioTemplateEntity;
import com.contextflow.scenario.domain.ScenarioTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenarioTemplateRepository extends JpaRepository<ScenarioTemplateEntity, Long> {

    List<ScenarioTemplateEntity> findByStatusOrderByDifficultyScoreAsc(ScenarioTemplateStatus status);
}
