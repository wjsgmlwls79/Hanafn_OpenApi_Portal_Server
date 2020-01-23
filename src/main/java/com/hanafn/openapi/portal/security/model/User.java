package com.hanafn.openapi.portal.security.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.hanafn.openapi.portal.security.model.audit.UserDateAudit;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "PORTAL_USER_INFO")
@Data
@EqualsAndHashCode(callSuper=false)
public class User extends UserDateAudit {
	private static final long serialVersionUID = -7675599777317582614L;

	@Id
	@Column(name="USER_KEY")
    private String userKey;
    
	@NotBlank
    @Size(max = 50)
    @Column(name="USER_ID")
    private String userId;
    
    @NotBlank
    @Size(max = 50)
    @Column(name="USER_NM")
    private String userNm;

    @NotBlank
    @Size(max = 256)
    @Column(name="USER_PWD")
    private String password;

    @Column(name="USER_STAT_CD")
    private String statCd;

    @Column(name="USER_APLV_STAT")
    private String userAplvStat;

    @Column(name="TMP_PWD_YN")
    private String tmpPasswordYn;

    @Size(max = 50)
    @Column(name="TMP_PWD")
    private String TMP_Password;

    @Size(max = 15)
    @Column(name="USER_TEL")
    private String userTel;
    
    @Size(max = 30)
    @Column(name="ENTR_CD")
    private String entrCd;

    @Size(max = 10)
    @Column(name="PORTAL_TOS_YN")
    private String portalTosYn;

    @Size(max = 10)
    @Column(name="PRIVACY_TOS_YN")
    private String privacyTosYn;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "PORTAL_USER_ROLE_INFO",
            joinColumns = @JoinColumn(name = "USER_KEY"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_CD"))
    private Set<Role> roles = new HashSet<>();

    public User() {

    }

    public User(String userKey, String userId, String userNm, String password) {
        this.userKey = userKey;
        this.userId = userId;
        this.userNm = userNm;
        this.password = password;
        this.statCd = "WAIT";
        this.userAplvStat = "WAIT";
        this.tmpPasswordYn = "N";
        this.portalTosYn = "Y";
        this.privacyTosYn = "Y";
    }
}