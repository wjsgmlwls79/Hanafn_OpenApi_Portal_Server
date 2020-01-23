package com.hanafn.openapi.portal.security.repository;

import com.hanafn.openapi.portal.security.model.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<Login, String> {
    Optional<Login> findByUserIdAndUserStatCd(String userId, String userStatCd);
}