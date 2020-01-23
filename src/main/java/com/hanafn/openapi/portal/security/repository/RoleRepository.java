package com.hanafn.openapi.portal.security.repository;

import java.util.Optional;

import com.hanafn.openapi.portal.security.model.Role;
import com.hanafn.openapi.portal.security.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}