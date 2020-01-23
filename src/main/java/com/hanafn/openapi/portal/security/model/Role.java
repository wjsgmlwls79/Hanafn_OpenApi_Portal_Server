package com.hanafn.openapi.portal.security.model;

import javax.persistence.*;

import org.hibernate.annotations.NaturalId;

import lombok.Data;

@Entity
@Table(name = "PORTAL_ROLE_INFO")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ROLE_CD")
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(name="ROLE_NM", length = 60)
    private RoleName name;

    public Role() {
    }
    
    public Role(RoleName name) {
        this.name = name;
    }
}