package com.hanafn.openapi.portal.security.repository;

import com.hanafn.openapi.portal.security.model.Useorg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UseorgRepository extends JpaRepository<Useorg, String> {
    Optional<Useorg> findByUseorgId(String useorgId);
}