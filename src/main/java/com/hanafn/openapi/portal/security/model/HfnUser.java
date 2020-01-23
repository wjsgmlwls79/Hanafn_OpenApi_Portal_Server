package com.hanafn.openapi.portal.security.model;

import com.hanafn.openapi.portal.security.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PORTAL_HFN_USER_INFO")
@Data
@EqualsAndHashCode(callSuper=false)
public class HfnUser extends UserDateAudit {
	private static final long serialVersionUID = -7675599777317582614L;

	@Id
	@Column(name="USER_KEY")
    private String userKey;

    @NotBlank
    @Size(max = 50)
    @Column(name="HFN_CD")
    private String hfnCd;

    @NotBlank
    @Size(max = 50)
    @Column(name="HFN_ID")
    private String hfnId;

    @NotBlank
    @Size(max = 50)
    @Column(name="USER_NM")
    private String userNm;

    @NotBlank
    @Size(max = 256)
    @Column(name="USER_PWD")
    private String password;

    @Column(name="DEPT_NM")
    private String deptNm;

    @Column(name="JOB_NM")
    private String jobNm;

    @Column(name="USER_STAT_CD")
    private String userStatCd;

    @Column(name="ACCESS_CD")
    private String accessCd;

    @Column(name="SIGN_USER_YN")
    private String signUserYn;

    @Column(name="SIGN_LEVEL")
    private String signLevel;

    @Column(name="TMP_PWD_YN")
    private String tmpPasswordYn;

    @Size(max = 50)
    @Column(name="TMP_PWD")
    private String TMP_Password;

    @Column(name="PWD_CHANGE_DT")
    private String pwdChangeDt;

    @Size(max = 15)
    @Column(name="USER_TEL")
    private String userTel;

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

    public HfnUser() {

    }

    public HfnUser(String userKey, String hfnCd, String hfnId, String userNm, String deptNm, String jobNm, String tmpPasswordYn,
                   String pwdChangeDt, String portalTosYn, String privacyTosYn) {
        this.userKey = userKey;
        this.hfnCd = hfnCd;
        this.hfnId = hfnId;
        this.userNm = userNm;
        this.deptNm = deptNm;
        this.jobNm = jobNm;
        this.tmpPasswordYn = tmpPasswordYn;
        this.pwdChangeDt = pwdChangeDt;
        this.portalTosYn = portalTosYn;
        this.privacyTosYn = privacyTosYn;
    }
}