package com.hanafn.openapi.portal.security.repository;

import com.hanafn.openapi.portal.security.model.HfnUser;
import com.hanafn.openapi.portal.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HfnUserRepository extends JpaRepository<HfnUser, String> {
    Optional<HfnUser> findByHfnCdAndHfnId(String hfnCd, String hfnId);
}