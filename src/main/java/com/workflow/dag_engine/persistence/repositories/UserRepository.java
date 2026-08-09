package com.workflow.dag_engine.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.workflow.dag_engine.persistence.entities.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUserNameAndPassword(String userName, String password);

    boolean existsByUserName(String userName);

    UserEntity findByUserNameAndPassword(String userName, String password);

    UserEntity findByUserName(String userName);

    @Query("SELECT userId FROM UserEntity WHERE userName = :userName AND password = :password")
    Long fetchUserIdByNameAndPassword(@Param("userName") String userName, @Param("password") String password);

}
