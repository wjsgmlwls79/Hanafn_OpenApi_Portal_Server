package com.hanafn.openapi.portal.security.model.audit;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@MappedSuperclass
@JsonIgnoreProperties(
        value = {"createdBy", "updatedBy"},
        allowGetters = true
)
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class UserDateAudit extends DateAudit {
	private static final long serialVersionUID = 3263542423645288667L;

	@CreatedBy
	@Column(name="REG_USER")
    private String createdBy;

    @LastModifiedBy
    @Column(name="MOD_USER")
    private String updatedBy;
}