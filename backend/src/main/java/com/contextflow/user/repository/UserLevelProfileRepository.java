package com.contextflow.user.repository;

import com.contextflow.user.domain.UserLevelProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLevelProfileRepository extends JpaRepository<UserLevelProfileEntity, Long> {

    Optional<UserLevelProfileEntity> findByUserId(Long userId);
}
