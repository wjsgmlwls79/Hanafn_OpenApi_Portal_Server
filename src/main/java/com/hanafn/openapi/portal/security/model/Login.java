package com.hanafn.openapi.portal.security.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "PORTAL_USER_LOGIN")
@Data
@EqualsAndHashCode(callSuper=false)
public class Login {
	private static final long serialVersionUID = -7675599777317582614L;

    @Column(name="USER_KEY")
    private String userKey;

	@Id
	@Column(name="USER_ID")
    private String userId;

    @NotBlank
    @Size(max = 256)
    @Column(name="USER_PWD")
    private String password;

    @Column(name="USER_TYPE")
    private String userType;

    @Column(name="USER_STAT_CD")
    private String userStatCd;

    public Login() {

    }

    public Login(String userKey, String userId, String password, String userType, String userStatCd) {
        this.userKey = userKey;
        this.userId = userId;
        this.password = password;
        this.userType = userType;
        this.userStatCd = "APLV";
    }
}