package com.hanafn.openapi.portal.security.repository;

import java.util.List;
import java.util.Optional;

import com.hanafn.openapi.portal.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserIdAndUserAplvStatNot(String userId, String userAplvStat);

}